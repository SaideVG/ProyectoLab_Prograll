package universidad.sigelab.view;

import universidad.sigelab.controller.UsuarioController;
import universidad.sigelab.model.Usuario;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Window;

/**
 * Pantalla de inicio de sesión.
 *
 * La interfaz se construye por código (no con el diseñador de formularios)
 * para poder aplicar la paleta de {@link Tema}, esquinas redondeadas y las
 * animaciones de entrada y de los botones.
 */
public class LoginForm {

    private JPanel panelPrincipal;
    private JLabel lblTitulo;
    private CampoTexto txtUsuario;
    private CampoPassword txtPassword;
    private BotonRedondeado btnIngresar;
    private BotonRedondeado btnSalir;
    private BotonRedondeado btnRegistro;

    /** La View solamente conoce al Controller. */
    private final UsuarioController usuarioController;

    public LoginForm() {
        this(new UsuarioController());
    }

    public LoginForm(UsuarioController usuarioController) {
        this.usuarioController = usuarioController;
        construirUI();
        configurarEventos();
    }

    /*
     * ========================================================
     * INTERFAZ
     * ========================================================
     */

    private void construirUI() {
        panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBackground(Tema.FONDO);
        // Margen alrededor de la tarjeta para que nunca toque los bordes de la ventana.
        panelPrincipal.setBorder(new EmptyBorder(28, 28, 28, 28));

        PanelRedondeado tarjeta = new PanelRedondeado();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(38, 36, 34, 36));

        lblTitulo = new JLabel("SIGELAB");
        lblTitulo.setFont(Tema.titulo());
        lblTitulo.setForeground(Tema.PRIMARIO);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblSubtitulo = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "Sistema de Gestión y Control<br>de Laboratorios Universitarios"
                        + "</div></html>");
        lblSubtitulo.setFont(Tema.subtitulo());
        lblSubtitulo.setForeground(Tema.TEXTO_SUAVE);
        lblSubtitulo.setHorizontalAlignment(SwingConstants.CENTER);

        anchoCompleto(lblTitulo);
        anchoCompleto(lblSubtitulo);

        JLabel lblUsuario = etiqueta("Nombre de usuario o código");
        txtUsuario = new CampoTexto(18);
        txtUsuario.setAnchoPreferido(300);

        JLabel lblPassword = etiqueta("Contraseña");
        txtPassword = new CampoPassword(18);
        txtPassword.setAnchoPreferido(300);

        btnIngresar = new BotonRedondeado("Ingresar", BotonRedondeado.Estilo.PRIMARIO);
        btnRegistro = new BotonRedondeado("Registro nuevo", BotonRedondeado.Estilo.SECUNDARIO);
        btnSalir = new BotonRedondeado("Salir", BotonRedondeado.Estilo.CLARO);

        anchoCompleto(txtUsuario);
        anchoCompleto(txtPassword);
        anchoCompleto(btnIngresar);
        anchoCompleto(btnRegistro);
        anchoCompleto(btnSalir);

        tarjeta.add(lblTitulo);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(lblSubtitulo);
        tarjeta.add(Box.createVerticalStrut(26));
        tarjeta.add(lblUsuario);
        tarjeta.add(Box.createVerticalStrut(6));
        tarjeta.add(txtUsuario);
        tarjeta.add(Box.createVerticalStrut(14));
        tarjeta.add(lblPassword);
        tarjeta.add(Box.createVerticalStrut(6));
        tarjeta.add(txtPassword);
        tarjeta.add(Box.createVerticalStrut(26));
        tarjeta.add(btnIngresar);
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(btnRegistro);
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(btnSalir);

        panelPrincipal.add(tarjeta);
    }

    private JLabel etiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(Tema.etiqueta());
        etiqueta.setForeground(Tema.TEXTO);
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        return etiqueta;
    }

    private void anchoCompleto(JComponent componente) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension preferido = componente.getPreferredSize();
        componente.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferido.height));
    }

    /*
     * ========================================================
     * VENTANA
     * ========================================================
     */

    public static void mostrar() {
        LoginForm loginForm = new LoginForm();

        JFrame frame = new JFrame("SIGELAB - Inicio de sesión");
        frame.setContentPane(loginForm.getPanelPrincipal());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        // pack() calcula el tamaño justo según el contenido, así nada queda cortado
        // aunque el sistema use escala (DPI) distinta.
        frame.pack();
        frame.setLocationRelativeTo(null);

        // Enter equivale a presionar Ingresar.
        frame.getRootPane().setDefaultButton(loginForm.btnIngresar);

        Animaciones.mostrarConDesvanecido(frame);
    }

    /*
     * ========================================================
     * EVENTOS
     * ========================================================
     */

    private void configurarEventos() {
        btnIngresar.addActionListener(e -> ingresar());
        btnSalir.addActionListener(e -> System.exit(0));

        btnRegistro.addActionListener(e -> {
            btnRegistro.destello();
            Window duenio = SwingUtilities.getWindowAncestor(panelPrincipal);
            RegistroUsuarioForm.abrir(duenio, usuarioController);
        });
    }

    private void ingresar() {
        try {
            Usuario usuario = usuarioController.iniciarSesion(
                    txtUsuario.getText().trim(),
                    new String(txtPassword.getPassword()));

            MainForm.mostrar(usuario);
            cerrarVentana();
        } catch (Exception e) {
            txtPassword.setText("");
            Dialogos.error(panelPrincipal, e);
        }
    }

    /** Cierra únicamente la ventana del login. */
    private void cerrarVentana() {
        Window ventanaActual = SwingUtilities.getWindowAncestor(panelPrincipal);
        if (ventanaActual != null) {
            ventanaActual.dispose();
        }
    }

    public JPanel getPanelPrincipal() {
        return panelPrincipal;
    }
}
