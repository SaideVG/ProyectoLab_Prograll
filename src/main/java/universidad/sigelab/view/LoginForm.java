package universidad.sigelab.view;

import universidad.sigelab.controller.UsuarioController;
import universidad.sigelab.model.Usuario;

import javax.swing.*;
import java.awt.*;

public class LoginForm {

    // Componentes vinculados desde LoginForm.form
    private JPanel panelPrincipal;

    private JLabel lblTitulo;

    private JTextField txtUsuario;
    private JPasswordField txtPassword;

    private JButton btnIngresar;
    private JButton btnSalir;

    // La View solamente conoce al Controller.
    private final UsuarioController usuarioController;


    /*
     * Constructor normal.
     */
    public LoginForm() {

        this(new UsuarioController());
    }


    /*
     * También permitimos recibir el Controller desde afuera.
     */
    public LoginForm(
            UsuarioController usuarioController
    ) {

        this.usuarioController = usuarioController;

        configurarFormulario();
        configurarEventos();
    }


    /*
     * Crea la ventana de inicio de sesión.
     * Se utiliza al arrancar la aplicación y al cerrar sesión.
     */
    public static void mostrar() {

        LoginForm loginForm =
                new LoginForm();

        JFrame frame =
                new JFrame(
                        "SIGELAB - Inicio de sesión"
                );

        frame.setContentPane(
                loginForm.getPanelPrincipal()
        );

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        frame.setSize(
                460,
                300
        );

        frame.setLocationRelativeTo(null);

        frame.setResizable(false);

        // Enter equivale a presionar Ingresar.
        frame.getRootPane().setDefaultButton(
                loginForm.btnIngresar
        );

        frame.setVisible(true);
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {

        lblTitulo.setFont(
                lblTitulo.getFont().deriveFont(
                        Font.BOLD,
                        22f
                )
        );
    }


    /*
     * Eventos de los controles.
     */
    private void configurarEventos() {

        btnIngresar.addActionListener(
                e -> ingresar()
        );

        btnSalir.addActionListener(
                e -> System.exit(0)
        );
    }


    /*
     * ========================================================
     * INICIAR SESIÓN
     * ========================================================
     */
    private void ingresar() {

        try {

            Usuario usuario =
                    usuarioController.iniciarSesion(
                            txtUsuario.getText().trim(),
                            new String(
                                    txtPassword.getPassword()
                            )
                    );

            MainForm.mostrar(usuario);

            cerrarVentana();

        } catch (Exception e) {

            txtPassword.setText("");

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * Cierra únicamente la ventana del login.
     */
    private void cerrarVentana() {

        Window ventanaActual =
                SwingUtilities.getWindowAncestor(
                        panelPrincipal
                );

        if (ventanaActual != null) {
            ventanaActual.dispose();
        }
    }


    /*
     * Todos los errores de la View pasan por un único método.
     */
    private void mostrarError(
            String mensaje
    ) {

        if (
                mensaje == null
                        || mensaje.isBlank()
        ) {

            mensaje =
                    "Ocurrió un error inesperado.";
        }

        JOptionPane.showMessageDialog(
                panelPrincipal,
                mensaje,
                "Inicio de sesión",
                JOptionPane.ERROR_MESSAGE
        );
    }


    public JPanel getPanelPrincipal() {

        return panelPrincipal;
    }
}
