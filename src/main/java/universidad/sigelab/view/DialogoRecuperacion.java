package universidad.sigelab.view;

import universidad.sigelab.controller.UsuarioController;
import universidad.sigelab.service.SolicitudRecuperacion;
import universidad.sigelab.util.BusinessException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Window;

/**
 * Diálogo de recuperación de contraseña con código de un solo uso.
 *
 * Paso 1: el usuario escribe su usuario o correo y se le envía un código (o se
 * muestra en pantalla si no hay SMTP configurado). Paso 2: escribe el código y
 * su nueva contraseña. Usa la misma paleta y los mismos componentes que el
 * login y el registro, para que todo se vea consistente.
 */
class DialogoRecuperacion {

    private static final int ANCHO_CAMPO = 320;
    private static final String PASO_DATOS = "datos";
    private static final String PASO_CODIGO = "codigo";

    private final JDialog dialogo;
    private final UsuarioController usuarioController;
    private final IndicadorConexion indicador = new IndicadorConexion();

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenedor = new JPanel(tarjetas);

    private JLabel lblSubtitulo;
    private JLabel lblAviso;

    private CampoTexto txtIdentificacion;
    private CampoTexto txtCodigo;
    private CampoPassword txtNueva;
    private CampoPassword txtConfirmar;

    private BotonRedondeado btnEnviar;
    private BotonRedondeado btnRestablecer;

    /** Dato con el que se inició la recuperación; se reutiliza en el paso 2. */
    private String identificacion;

    static void abrir(Window duenio, UsuarioController usuarioController) {
        new DialogoRecuperacion(duenio, usuarioController).dialogo.setVisible(true);
    }

    private DialogoRecuperacion(Window duenio, UsuarioController usuarioController) {
        this.usuarioController = usuarioController;

        dialogo = new JDialog(duenio, "SIGELAB - Recuperar contraseña", Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialogo.setContentPane(construirContenido());
        dialogo.setResizable(false);
        dialogo.pack();
        dialogo.setLocationRelativeTo(duenio);
        dialogo.getRootPane().setDefaultButton(btnEnviar);

        indicador.verificar();
        SwingUtilities.invokeLater(() -> txtIdentificacion.requestFocusInWindow());
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
        tarjeta.setBorder(new EmptyBorder(28, 36, 16, 36));

        JLabel titulo = new JLabel("Recuperar contraseña", SwingConstants.CENTER);
        titulo.setFont(Tema.titulo().deriveFont(24f));
        titulo.setForeground(Tema.PRIMARIO);

        lblSubtitulo = new JLabel(
                "<html><div style='text-align:center;'>Escriba su usuario o correo y le enviaremos<br>"
                        + "un código de verificación.</div></html>",
                SwingConstants.CENTER);
        lblSubtitulo.setFont(Tema.subtitulo());
        lblSubtitulo.setForeground(Tema.TEXTO_SUAVE);

        JPanel encabezado = new JPanel();
        encabezado.setOpaque(false);
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        anchoCompleto(titulo);
        anchoCompleto(lblSubtitulo);
        encabezado.add(titulo);
        encabezado.add(Box.createVerticalStrut(4));
        encabezado.add(lblSubtitulo);

        contenedor.setOpaque(false);
        contenedor.add(crearPasoDatos(), PASO_DATOS);
        contenedor.add(crearPasoCodigo(), PASO_CODIGO);

        tarjeta.add(encabezado, BorderLayout.NORTH);
        tarjeta.add(contenedor, BorderLayout.CENTER);
        tarjeta.add(crearPie(), BorderLayout.SOUTH);

        fondo.add(tarjeta);
        return fondo;
    }

    private JPanel crearPasoDatos() {
        JPanel paso = new JPanel();
        paso.setOpaque(false);
        paso.setLayout(new BoxLayout(paso, BoxLayout.Y_AXIS));
        paso.setBorder(new EmptyBorder(26, 0, 0, 0));

        txtIdentificacion = new CampoTexto(18);
        txtIdentificacion.setAnchoPreferido(ANCHO_CAMPO);
        txtIdentificacion.setPlaceholder("Ej. ojperez o correo@universidad.edu");

        JLabel aviso = new JLabel(
                "<html><div style='text-align:center;'>El código confirma que la cuenta es suya.<br>"
                        + "Vence a los 10 minutos y solo sirve una vez.</div></html>",
                SwingConstants.CENTER);
        aviso.setFont(Tema.pie());
        aviso.setForeground(Tema.TEXTO_SUAVE);

        btnEnviar = new BotonRedondeado("Enviar código", BotonRedondeado.Estilo.PRIMARIO);
        BotonRedondeado btnCancelar = new BotonRedondeado("Cancelar", BotonRedondeado.Estilo.TEXTO);
        btnEnviar.addActionListener(e -> enviarCodigo());
        btnCancelar.addActionListener(e -> dialogo.dispose());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        botones.setOpaque(false);
        botones.add(btnEnviar);
        botones.add(btnCancelar);

        JComponent bloqueCampo = bloqueCampo("USUARIO O CORREO", txtIdentificacion);
        anchoCompleto(bloqueCampo);
        anchoCompleto(aviso);
        anchoCompleto(botones);

        paso.add(bloqueCampo);
        paso.add(Box.createVerticalStrut(14));
        paso.add(aviso);
        paso.add(Box.createVerticalStrut(18));
        paso.add(botones);
        return paso;
    }

    private JPanel crearPasoCodigo() {
        JPanel paso = new JPanel();
        paso.setOpaque(false);
        paso.setLayout(new BoxLayout(paso, BoxLayout.Y_AXIS));
        paso.setBorder(new EmptyBorder(22, 0, 0, 0));

        lblAviso = new JLabel(" ", SwingConstants.CENTER);
        lblAviso.setFont(Tema.texto());

        txtCodigo = new CampoTexto(18);
        txtCodigo.setAnchoPreferido(ANCHO_CAMPO);
        txtCodigo.setPlaceholder("Ej. 482913");

        txtNueva = new CampoPassword(18);
        txtNueva.setAnchoPreferido(ANCHO_CAMPO);

        txtConfirmar = new CampoPassword(18);
        txtConfirmar.setAnchoPreferido(ANCHO_CAMPO);

        btnRestablecer = new BotonRedondeado("Restablecer contraseña", BotonRedondeado.Estilo.PRIMARIO);
        BotonRedondeado btnVolver = new BotonRedondeado("Volver", BotonRedondeado.Estilo.TEXTO);
        btnRestablecer.addActionListener(e -> restablecer());
        btnVolver.addActionListener(e -> volverAlInicio());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        botones.setOpaque(false);
        botones.add(btnRestablecer);
        botones.add(btnVolver);

        JLabel obligatorios = new JLabel("Todos los campos son obligatorios", SwingConstants.RIGHT);
        obligatorios.setFont(Tema.etiquetaMedia());
        obligatorios.setForeground(Tema.TEXTO_SUAVE);

        JComponent bloqueCodigo = bloqueCampo("CÓDIGO", txtCodigo);
        JComponent bloqueNueva = bloqueCampo("NUEVA CONTRASEÑA", txtNueva);
        JComponent bloqueConfirmar = bloqueCampo("CONFIRMAR CONTRASEÑA", txtConfirmar);

        anchoCompleto(lblAviso);
        anchoCompleto(bloqueCodigo);
        anchoCompleto(bloqueNueva);
        anchoCompleto(bloqueConfirmar);
        anchoCompleto(obligatorios);
        anchoCompleto(botones);

        paso.add(lblAviso);
        paso.add(Box.createVerticalStrut(16));
        paso.add(bloqueCodigo);
        paso.add(Box.createVerticalStrut(14));
        paso.add(bloqueNueva);
        paso.add(Box.createVerticalStrut(14));
        paso.add(bloqueConfirmar);
        paso.add(Box.createVerticalStrut(10));
        paso.add(obligatorios);
        paso.add(Box.createVerticalStrut(14));
        paso.add(botones);
        return paso;
    }

    private JPanel crearPie() {
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, Tema.BORDE),
                new EmptyBorder(12, 0, 0, 0)));

        JLabel version = new JLabel("SIGELAB v2.4");
        version.setFont(Tema.pie());
        version.setForeground(Tema.TEXTO_SUAVE);

        pie.add(indicador, BorderLayout.WEST);
        pie.add(version, BorderLayout.EAST);
        return pie;
    }

    /** Etiqueta encima del campo, igual que en el login. */
    private JComponent bloqueCampo(String textoEtiqueta, JComponent campo) {
        JPanel bloque = new JPanel();
        bloque.setOpaque(false);
        bloque.setLayout(new BoxLayout(bloque, BoxLayout.Y_AXIS));

        JLabel etiqueta = new JLabel(textoEtiqueta);
        etiqueta.setFont(Tema.etiqueta());
        etiqueta.setForeground(Tema.ETIQUETA);
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        etiqueta.setBorder(new EmptyBorder(0, 2, 0, 0));

        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, campo.getPreferredSize().height));

        bloque.add(etiqueta);
        bloque.add(Box.createVerticalStrut(7));
        bloque.add(campo);
        return bloque;
    }

    private void anchoCompleto(JComponent componente) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        componente.setMaximumSize(new Dimension(Integer.MAX_VALUE, componente.getPreferredSize().height));
    }

    /* ========================================================
       ACCIONES
       ======================================================== */

    private void enviarCodigo() {
        identificacion = txtIdentificacion.getText().trim();
        if (identificacion.isEmpty()) {
            Dialogos.advertir(dialogo, "Ingrese su usuario o correo.");
            txtIdentificacion.requestFocusInWindow();
            return;
        }

        btnEnviar.setEnabled(false);
        btnEnviar.setText("Enviando…");
        dialogo.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // El envío puede tardar (red): se hace fuera del hilo de la interfaz para no congelarla.
        new SwingWorker<SolicitudRecuperacion, Void>() {
            @Override
            protected SolicitudRecuperacion doInBackground() {
                return usuarioController.solicitarCodigoRecuperacion(identificacion);
            }

            @Override
            protected void done() {
                btnEnviar.setEnabled(true);
                btnEnviar.setText("Enviar código");
                dialogo.setCursor(Cursor.getDefaultCursor());
                try {
                    mostrarPasoCodigo(get());
                } catch (Exception e) {
                    Dialogos.error(dialogo, desenvuelta(e));
                }
            }
        }.execute();
    }

    private void mostrarPasoCodigo(SolicitudRecuperacion solicitud) {
        if (solicitud.isEnviadoPorCorreo()) {
            lblAviso.setText("<html><div style='text-align:center;'>Enviamos un código de verificación a <b>"
                    + solicitud.getCorreoEnmascarado() + "</b>.<br>Revise su correo e ingréselo aquí abajo.</div></html>");
            lblAviso.setForeground(Tema.TEXTO_SUAVE);
        } else {
            lblAviso.setText("<html><div style='text-align:center;'>No hay correo configurado. "
                    + "Su código de verificación es:<br><span style='font-size:18pt;'><b>"
                    + solicitud.getCodigoEnPantalla() + "</b></span></div></html>");
            lblAviso.setForeground(Tema.PRIMARIO);
        }

        lblSubtitulo.setText("Escriba el código recibido y su nueva contraseña.");
        txtCodigo.setText("");
        txtNueva.setText("");
        txtConfirmar.setText("");

        tarjetas.show(contenedor, PASO_CODIGO);
        dialogo.getRootPane().setDefaultButton(btnRestablecer);
        SwingUtilities.invokeLater(() -> txtCodigo.requestFocusInWindow());
    }

    private void restablecer() {
        try {
            String codigo = txtCodigo.getText().trim();
            String nueva = new String(txtNueva.getPassword());
            String confirmar = new String(txtConfirmar.getPassword());

            if (codigo.isEmpty()) throw new BusinessException("Ingrese el código que recibió.");
            if (!nueva.equals(confirmar)) throw new BusinessException("Las contraseñas no coinciden.");

            usuarioController.restablecerPassword(identificacion, codigo, nueva);
            Dialogos.informar(dialogo,
                    "Contraseña restablecida correctamente.\nYa puede iniciar sesión con su nueva contraseña.",
                    "Recuperar contraseña");
            dialogo.dispose();
        } catch (Exception e) {
            Dialogos.error(dialogo, e);
        }
    }

    private void volverAlInicio() {
        lblSubtitulo.setText("<html><div style='text-align:center;'>Escriba su usuario o correo y le enviaremos<br>"
                + "un código de verificación.</div></html>");
        tarjetas.show(contenedor, PASO_DATOS);
        dialogo.getRootPane().setDefaultButton(btnEnviar);
        SwingUtilities.invokeLater(() -> txtIdentificacion.requestFocusInWindow());
    }

    /** Cuando el trabajo corre en segundo plano, el error real viene envuelto. */
    private Exception desenvuelta(Exception e) {
        Throwable causa = e.getCause() != null ? e.getCause() : e;
        return causa instanceof Exception ex ? ex : e;
    }
}
