package universidad.sigelab.view;

import universidad.sigelab.controller.UsuarioController;
import universidad.sigelab.model.Usuario;
import universidad.sigelab.util.BusinessException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

/**
 * Formulario para dar de alta un usuario nuevo.
 *
 * Se abre desde el botón "Registro nuevo" del login y usa la misma paleta
 * ({@link Tema}) y los mismos componentes redondeados, para que las dos
 * pantallas se vean consistentes.
 */
class RegistroUsuarioForm {

    private static final int ANCHO_CAMPO = 300;

    private final JDialog dialogo;
    private final UsuarioController usuarioController;
    private final IndicadorConexion indicador = new IndicadorConexion();

    private CampoTexto txtNombre;
    private CampoTexto txtApellido;
    private CampoTexto txtUsuario;
    private CampoTexto txtCorreo;
    private CampoPassword txtPassword;
    private CampoPassword txtConfirmar;

    private BotonRedondeado btnRegistrar;

    /** Abre el formulario de forma modal sobre la ventana indicada. */
    static void abrir(Window duenio, UsuarioController usuarioController) {
        new RegistroUsuarioForm(duenio, usuarioController).dialogo.setVisible(true);
    }

    private RegistroUsuarioForm(Window duenio, UsuarioController usuarioController) {
        this.usuarioController = usuarioController;

        dialogo = new JDialog(duenio, "SIGELAB - Registro de usuarios", Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialogo.setContentPane(construirContenido());
        dialogo.setResizable(false);
        // pack() ajusta el diálogo al contenido para que no se corte nada.
        dialogo.pack();
        dialogo.setLocationRelativeTo(duenio);
        dialogo.getRootPane().setDefaultButton(btnRegistrar);

        indicador.verificar();
        SwingUtilities.invokeLater(() -> txtNombre.requestFocusInWindow());
    }

    /*
     * ========================================================
     * INTERFAZ
     * ========================================================
     */

    private JPanel construirContenido() {
        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(Tema.FONDO);
        // Margen alrededor de la tarjeta para que nunca toque los bordes de la ventana.
        fondo.setBorder(new EmptyBorder(22, 22, 22, 22));

        PanelRedondeado tarjeta = new PanelRedondeado();
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(new EmptyBorder(28, 36, 16, 36));

        JLabel titulo = new JLabel("Registro de usuarios", SwingConstants.CENTER);
        titulo.setFont(Tema.titulo());
        titulo.setForeground(Tema.PRIMARIO);

        JLabel subtitulo = new JLabel("Complete los datos para crear una cuenta", SwingConstants.CENTER);
        subtitulo.setFont(Tema.subtitulo());
        subtitulo.setForeground(Tema.TEXTO_SUAVE);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);

        txtNombre = new CampoTexto(18);
        txtApellido = new CampoTexto(18);
        txtUsuario = new CampoTexto(18);
        txtCorreo = new CampoTexto(18);
        txtPassword = new CampoPassword(18);
        txtConfirmar = new CampoPassword(18);

        agregarFila(formulario, 0, "Nombre", txtNombre, "Ej. Juan");
        agregarFila(formulario, 1, "Apellido", txtApellido, "Ej. Pérez");
        agregarFila(formulario, 2, "Nombre de usuario", txtUsuario, "jperez_lab");
        agregarFila(formulario, 3, "Correo", txtCorreo, "usuario@universidad.edu");
        agregarFila(formulario, 4, "Contraseña", txtPassword, null);
        agregarFila(formulario, 5, "Confirmar contraseña", txtConfirmar, null);

        JLabel obligatorios = new JLabel("Todos los campos son obligatorios", SwingConstants.RIGHT);
        obligatorios.setFont(Tema.etiquetaMedia());
        obligatorios.setForeground(Tema.TEXTO_SUAVE);

        btnRegistrar = new BotonRedondeado("Registrar", BotonRedondeado.Estilo.PRIMARIO);
        BotonRedondeado btnLimpiar = new BotonRedondeado("Limpiar", BotonRedondeado.Estilo.SECUNDARIO);
        BotonRedondeado btnCancelar = new BotonRedondeado("Cancelar", BotonRedondeado.Estilo.TEXTO);

        btnRegistrar.addActionListener(e -> registrar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnCancelar.addActionListener(e -> dialogo.dispose());

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        derecha.setOpaque(false);
        derecha.add(btnLimpiar);
        derecha.add(btnCancelar);

        JPanel botones = new JPanel(new BorderLayout());
        botones.setOpaque(false);
        botones.add(btnRegistrar, BorderLayout.WEST);
        botones.add(derecha, BorderLayout.EAST);

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        anchoCompleto(titulo);
        anchoCompleto(subtitulo);
        anchoCompleto(formulario);
        anchoCompleto(obligatorios);
        anchoCompleto(botones);

        contenido.add(titulo);
        contenido.add(Box.createVerticalStrut(4));
        contenido.add(subtitulo);
        contenido.add(Box.createVerticalStrut(26));
        contenido.add(formulario);
        contenido.add(Box.createVerticalStrut(14));
        contenido.add(obligatorios);
        contenido.add(Box.createVerticalStrut(16));
        contenido.add(botones);

        tarjeta.add(contenido, BorderLayout.CENTER);
        tarjeta.add(crearPie(), BorderLayout.SOUTH);

        fondo.add(tarjeta);
        return fondo;
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

    private void agregarFila(JPanel panel, int fila, String textoEtiqueta, JComponent campo, String placeholder) {
        GridBagConstraints texto = new GridBagConstraints();
        texto.gridx = 0;
        texto.gridy = fila;
        texto.anchor = GridBagConstraints.LINE_END;
        texto.insets = new Insets(7, 0, 7, 14);

        JLabel etiqueta = new JLabel(textoEtiqueta);
        etiqueta.setFont(Tema.etiquetaMedia());
        etiqueta.setForeground(Tema.ETIQUETA);
        panel.add(etiqueta, texto);

        GridBagConstraints contenedor = new GridBagConstraints();
        contenedor.gridx = 1;
        contenedor.gridy = fila;
        contenedor.weightx = 1;
        contenedor.fill = GridBagConstraints.HORIZONTAL;
        contenedor.insets = new Insets(7, 0, 7, 0);

        if (campo instanceof CampoTexto campoTexto) {
            campoTexto.setAnchoPreferido(ANCHO_CAMPO);
            campoTexto.setPlaceholder(placeholder);
        } else if (campo instanceof CampoPassword campoPassword) {
            campoPassword.setAnchoPreferido(ANCHO_CAMPO);
            campoPassword.setPlaceholder(placeholder);
        }
        panel.add(campo, contenedor);
    }

    /** Hace que un componente ocupe todo el ancho disponible dentro de la tarjeta. */
    private void anchoCompleto(JComponent componente) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        componente.setMaximumSize(new Dimension(Integer.MAX_VALUE, componente.getPreferredSize().height));
    }

    /*
     * ========================================================
     * ACCIONES
     * ========================================================
     */

    private void registrar() {
        try {
            String password = new String(txtPassword.getPassword());
            String confirmacion = new String(txtConfirmar.getPassword());

            if (!password.equals(confirmacion)) {
                throw new BusinessException("Las contraseñas no coinciden.");
            }

            Usuario usuario = new Usuario(
                    txtNombre.getText(),
                    txtApellido.getText(),
                    txtUsuario.getText(),
                    null,
                    txtCorreo.getText(),
                    null);

            if (usuarioController.registrar(usuario, password)) {
                Dialogos.informar(dialogo,
                        "Usuario registrado correctamente. Ya puede iniciar sesión.",
                        "Registro de usuarios");
                dialogo.dispose();
            } else {
                Dialogos.error(dialogo, "No se pudo registrar el usuario.");
            }
        } catch (Exception e) {
            Dialogos.error(dialogo, e);
        }
    }

    private void limpiar() {
        txtNombre.setText("");
        txtApellido.setText("");
        txtUsuario.setText("");
        txtCorreo.setText("");
        txtPassword.setText("");
        txtConfirmar.setText("");
        txtNombre.requestFocusInWindow();
    }
}
