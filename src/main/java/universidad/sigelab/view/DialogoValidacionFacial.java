package universidad.sigelab.view;

import universidad.sigelab.model.Usuario;
import universidad.sigelab.service.ReconocimientoFacial;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Segundo factor del login (RN-02): verifica en vivo que el rostro capturado
 * sea el mismo que el usuario registró.
 *
 * Compara la firma facial capturada con la guardada usando la similitud del
 * coseno de SFace. Da hasta {@link #INTENTOS_MAXIMOS} intentos antes de negar
 * el acceso.
 */
class DialogoValidacionFacial {

    private static final int INTENTOS_MAXIMOS = 3;

    private final JDialog ventana;
    private final Usuario usuario;
    private final float[] rostroRegistrado;

    private final PanelCamara camara = new PanelCamara();
    private final JLabel estado = new JLabel(" ", SwingConstants.CENTER);
    private final BotonRedondeado btnVerificar =
            new BotonRedondeado("Verificar rostro", BotonRedondeado.Estilo.PRIMARIO);
    private final Timer vigilante;
    private final boolean hayCamara;

    private int intentos;
    private boolean aprobado;

    static boolean aprobar(Window duenio, Usuario usuario, float[] rostroRegistrado) {
        DialogoValidacionFacial dialogo = new DialogoValidacionFacial(duenio, usuario, rostroRegistrado);
        dialogo.ventana.setVisible(true);
        return dialogo.aprobado;
    }

    private DialogoValidacionFacial(Window duenio, Usuario usuario, float[] rostroRegistrado) {
        this.usuario = usuario;
        this.rostroRegistrado = rostroRegistrado;

        ventana = new JDialog(duenio, "SIGELAB - Verificación facial", Dialog.ModalityType.APPLICATION_MODAL);
        ventana.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        ventana.setContentPane(construirContenido());
        ventana.setResizable(false);

        hayCamara = ReconocimientoFacial.disponible() && camara.abrir();
        if (hayCamara) {
            camara.iniciar();
        } else {
            estado.setText("El reconocimiento facial no está disponible en este equipo.");
            estado.setForeground(Tema.PELIGRO);
            btnVerificar.setEnabled(false);
        }

        vigilante = new Timer(250, e -> actualizarEstado());
        vigilante.start();

        ventana.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                cerrar();
            }
        });

        ventana.pack();
        ventana.setLocationRelativeTo(duenio);
    }

    /* ========================================================
       INTERFAZ
       ======================================================== */

    private JPanel construirContenido() {
        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(Tema.FONDO);
        fondo.setBorder(new EmptyBorder(22, 22, 22, 22));

        PanelRedondeado tarjeta = new PanelRedondeado();
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(new EmptyBorder(24, 30, 20, 30));

        JLabel titulo = new JLabel("Verificación facial", SwingConstants.CENTER);
        titulo.setFont(Tema.titulo().deriveFont(23f));
        titulo.setForeground(Tema.PRIMARIO);

        JLabel subtitulo = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "Usuario: <b>" + usuario.getNombreCompleto() + "</b><br>"
                        + "Mire a la cámara y presione <b>Verificar rostro</b>."
                        + "</div></html>",
                SwingConstants.CENTER);
        subtitulo.setFont(Tema.subtitulo());
        subtitulo.setForeground(Tema.TEXTO_SUAVE);

        BotonRedondeado btnCancelar = new BotonRedondeado("Cancelar", BotonRedondeado.Estilo.TEXTO);
        btnVerificar.addActionListener(e -> verificar());
        btnCancelar.addActionListener(e -> ventana.dispose());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        botones.setOpaque(false);
        botones.add(btnVerificar);
        botones.add(btnCancelar);

        JPanel vista = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        vista.setOpaque(false);
        vista.add(camara);

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        anchoCompleto(titulo);
        anchoCompleto(subtitulo);
        anchoCompleto(vista);
        anchoCompleto(estado);
        anchoCompleto(botones);

        contenido.add(titulo);
        contenido.add(Box.createVerticalStrut(4));
        contenido.add(subtitulo);
        contenido.add(Box.createVerticalStrut(16));
        contenido.add(vista);
        contenido.add(Box.createVerticalStrut(12));
        contenido.add(estado);
        contenido.add(Box.createVerticalStrut(14));
        contenido.add(botones);

        tarjeta.add(contenido, BorderLayout.CENTER);
        fondo.add(tarjeta);
        return fondo;
    }

    private void anchoCompleto(JComponent componente) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        componente.setMaximumSize(new Dimension(Integer.MAX_VALUE, componente.getPreferredSize().height));
    }

    /* ========================================================
       ACCIONES
       ======================================================== */

    private void actualizarEstado() {
        if (!hayCamara || intentos > 0) return;
        if (camara.hayRostro()) {
            estado.setText("Rostro detectado. Ya puede verificar.");
            estado.setForeground(Tema.EXITO);
        } else {
            estado.setText("Buscando rostro… Mire a la cámara.");
            estado.setForeground(Tema.TEXTO_SUAVE);
        }
    }

    private void verificar() {
        try {
            float[] vector = camara.vectorDelRostro();
            if (vector == null) {
                Dialogos.advertir(ventana, "No se detecta ningún rostro. Mire a la cámara e intente de nuevo.");
                return;
            }
            double similitud = ReconocimientoFacial.similitud(rostroRegistrado, vector);
            if (similitud >= ReconocimientoFacial.UMBRAL_SIMILITUD) {
                aprobado = true;
                ventana.dispose();
            } else {
                intentos++;
                int restantes = INTENTOS_MAXIMOS - intentos;
                if (restantes <= 0) {
                    Dialogos.error(ventana, "No se pudo verificar su rostro tras " + INTENTOS_MAXIMOS + " intentos.");
                    ventana.dispose();
                } else {
                    estado.setText("No coincide. Intentos restantes: " + restantes + ".");
                    estado.setForeground(Tema.PELIGRO);
                }
            }
        } catch (Exception e) {
            Dialogos.error(ventana, e);
        }
    }

    private void cerrar() {
        vigilante.stop();
        camara.cerrar();
    }
}
