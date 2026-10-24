package universidad.sigelab.view;

import universidad.sigelab.controller.UsuarioController;
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Diálogo para registrar (o volver a registrar) el rostro de un usuario.
 *
 * Muestra la cámara en vivo y permite capturar en cualquier momento; al capturar
 * calcula la firma facial y la guarda en la base.
 */
class DialogoRegistroRostro {

    private final JDialog ventana;
    private final UsuarioController usuarioController;
    private final Usuario usuario;

    private final PanelCamara camara = new PanelCamara();
    private final JLabel estado = new JLabel(" ", SwingConstants.CENTER);
    private final BotonRedondeado btnCapturar =
            new BotonRedondeado("Capturar rostro", BotonRedondeado.Estilo.PRIMARIO);
    private final Timer vigilante;
    private final boolean hayCamara;

    private boolean registrado;

    static boolean abrir(Window duenio, UsuarioController usuarioController, Usuario usuario) {
        DialogoRegistroRostro dialogo = new DialogoRegistroRostro(duenio, usuarioController, usuario);
        dialogo.ventana.setVisible(true);
        return dialogo.registrado;
    }

    private DialogoRegistroRostro(Window duenio, UsuarioController usuarioController, Usuario usuario) {
        this.usuarioController = usuarioController;
        this.usuario = usuario;

        ventana = new JDialog(duenio, "SIGELAB - Registro de rostro", Dialog.ModalityType.APPLICATION_MODAL);
        ventana.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        ventana.setContentPane(construirContenido());
        ventana.setResizable(false);

        hayCamara = ReconocimientoFacial.disponible() && camara.abrir();
        if (hayCamara) {
            camara.iniciar();
        } else {
            estado.setText("No se detectó ninguna cámara en este equipo.");
            estado.setForeground(Tema.PELIGRO);
            btnCapturar.setEnabled(false);
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
        fondo.setBackground(new Color(0x06, 0x0B, 0x14));
        fondo.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel tarjeta = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x0A, 0x12, 0x24));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(Tema.BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        tarjeta.setOpaque(false);
        tarjeta.setBorder(new EmptyBorder(24, 30, 20, 30));

        JLabel titulo = new JLabel("Registro facial", SwingConstants.CENTER);
        titulo.setFont(Tema.titulo().deriveFont(23f));
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "Centre su rostro dentro del recuadro verde y presione <b>Capturar rostro</b>."
                        + "</div></html>",
                SwingConstants.CENTER);
        subtitulo.setFont(Tema.subtitulo());
        subtitulo.setForeground(Tema.TEXTO_SUAVE);

        BotonRedondeado btnCancelar = new BotonRedondeado("Cancelar", BotonRedondeado.Estilo.TEXTO);
        btnCapturar.addActionListener(e -> capturar());
        btnCancelar.addActionListener(e -> ventana.dispose());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        botones.setOpaque(false);
        botones.add(btnCapturar);
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
        if (!hayCamara) return;
        if (camara.hayRostro()) {
            estado.setText("Rostro detectado. Ya puede capturar.");
            estado.setForeground(Tema.EXITO);
        } else {
            estado.setText("Buscando rostro… Mire a la cámara.");
            estado.setForeground(Tema.TEXTO_SUAVE);
        }
    }

    private void capturar() {
        try {
            float[] vector = camara.vectorDelRostro();
            if (vector == null) {
                Dialogos.advertir(ventana, "No se detecta ningún rostro. Mire a la cámara e intente de nuevo.");
                return;
            }
            usuarioController.registrarRostro(usuario, vector);
            registrado = true;
            Dialogos.informar(ventana, "Rostro registrado correctamente.", "Registro facial");
            ventana.dispose();
        } catch (Exception e) {
            Dialogos.error(ventana, e);
        }
    }

    private void cerrar() {
        vigilante.stop();
        camara.cerrar();
    }
}