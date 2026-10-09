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
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;

/**
 * Formulario para dar de alta un usuario nuevo.
 *
 * Se abre desde el botón "Registro nuevo" del login. Usa la misma paleta que
 * el login (Tema) y los mismos botones/campos redondeados para que las dos
 * pantallas se vean consistentes.
 */
class RegistroUsuarioForm {

    private final JDialog dialogo;
    private final UsuarioController usuarioController;

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
        fondo.setBorder(new EmptyBorder(24, 24, 24, 24));

        PanelRedondeado tarjeta = new PanelRedondeado();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(30, 34, 28, 34));

        JLabel titulo = new JLabel("Registro de usuarios");
        titulo.setFont(Tema.titulo());
        titulo.setForeground(Tema.PRIMARIO);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitulo = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "Complete los datos<br>para crear una cuenta"
                        + "</div></html>");
        subtitulo.setFont(Tema.subtitulo());
        subtitulo.setForeground(Tema.TEXTO_SUAVE);
        subtitulo.setHorizontalAlignment(SwingConstants.CENTER);

        anchoCompleto(titulo);
        anchoCompleto(subtitulo);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);

        txtNombre = new CampoTexto(18);
        txtApellido = new CampoTexto(18);
        txtUsuario = new CampoTexto(18);
        txtCorreo = new CampoTexto(18);
        txtPassword = new CampoPassword(18);
        txtConfirmar = new CampoPassword(18);

        agregarFila(formulario, 0, "Nombre", txtNombre);
        agregarFila(formulario, 1, "Apellido", txtApellido);
        agregarFila(formulario, 2, "Nombre de usuario", txtUsuario);
        agregarFila(formulario, 3, "Correo", txtCorreo);
        agregarFila(formulario, 4, "Contraseña", txtPassword);
        agregarFila(formulario, 5, "Confirmar contraseña", txtConfirmar);

        btnRegistrar = new BotonRedondeado("Registrar", BotonRedondeado.Estilo.PRIMARIO);
        BotonRedondeado btnLimpiar = new BotonRedondeado("Limpiar", BotonRedondeado.Estilo.CLARO);
        BotonRedondeado btnCancelar = new BotonRedondeado("Cancelar", BotonRedondeado.Estilo.CLARO);

        btnRegistrar.addActionListener(e -> registrar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnCancelar.addActionListener(e -> dialogo.dispose());

        JPanel botones = new JPanel(new GridLayout(1, 3, 10, 0));
        botones.setOpaque(false);
        botones.add(btnRegistrar);
        botones.add(btnLimpiar);
        botones.add(btnCancelar);

        anchoCompleto(formulario);
        anchoCompleto(botones);

        tarjeta.add(titulo);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(subtitulo);
        tarjeta.add(Box.createVerticalStrut(24));
        tarjeta.add(formulario);
        tarjeta.add(Box.createVerticalStrut(22));
        tarjeta.add(botones);

        fondo.add(tarjeta);
        return fondo;
    }

    private void agregarFila(JPanel panel, int fila, String textoEtiqueta, Component campo) {
        GridBagConstraints texto = new GridBagConstraints();
        texto.gridx = 0;
        texto.gridy = fila;
        texto.anchor = GridBagConstraints.LINE_END;
        texto.insets = new Insets(6, 0, 6, 12);

        JLabel etiqueta = new JLabel(textoEtiqueta);
        etiqueta.setFont(Tema.etiqueta());
        etiqueta.setForeground(Tema.TEXTO);
        panel.add(etiqueta, texto);

        GridBagConstraints contenedor = new GridBagConstraints();
        contenedor.gridx = 1;
        contenedor.gridy = fila;
        contenedor.weightx = 1;
        contenedor.fill = GridBagConstraints.HORIZONTAL;
        contenedor.insets = new Insets(6, 0, 6, 0);
        int anchoCampo = 230;
        if (campo instanceof CampoTexto campoTexto) {
            campoTexto.setAnchoPreferido(anchoCampo);
        } else if (campo instanceof CampoPassword campoPassword) {
            campoPassword.setAnchoPreferido(anchoCampo);
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
