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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Window;

/**
 * Pantalla de inicio de sesión.
 *
 * La interfaz se construye por código (no con el diseñador de formularios) para
 * poder aplicar la paleta de {@link Tema}, esquinas redondeadas y animaciones.
 * Es una pantalla dividida: a la izquierda el formulario y a la derecha una
 * ilustración decorativa.
 */
public class LoginForm {

    private static final int ANCHO_PANEL = 400;
    private static final int ALTO_PANEL = 620;
    private static final int ANCHO_CAMPO = 300;

    private JPanel panelPrincipal;
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
        panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(Tema.SUPERFICIE);
        panelPrincipal.setPreferredSize(new Dimension(ANCHO_PANEL + 520, ALTO_PANEL));

        panelPrincipal.add(crearPanelFormulario(), BorderLayout.WEST);
        panelPrincipal.add(new PanelIlustracion(), BorderLayout.CENTER);
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Tema.SUPERFICIE);
        panel.setPreferredSize(new Dimension(ANCHO_PANEL, ALTO_PANEL));

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(new EmptyBorder(0, 46, 0, 46));

        JLabel lblTitulo = new JLabel("SIGELAB", SwingConstants.CENTER);
        lblTitulo.setFont(Tema.titulo());
        lblTitulo.setForeground(Tema.PRIMARIO);

        JLabel lblSubtitulo = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "Sistema de Gestión y Control<br>de Laboratorios Universitarios"
                        + "</div></html>",
                SwingConstants.CENTER);
        lblSubtitulo.setFont(Tema.subtitulo());
        lblSubtitulo.setForeground(Tema.TEXTO_SUAVE);

        JLabel lblUsuario = etiqueta("NOMBRE DE USUARIO O CÓDIGO");
        txtUsuario = new CampoTexto(18);
        txtUsuario.setAnchoPreferido(ANCHO_CAMPO);
        txtUsuario.setPlaceholder("Ej. 020240182 ojperez");

        JLabel lblPassword = etiqueta("CONTRASEÑA");
        txtPassword = new CampoPassword(18);
        txtPassword.setAnchoPreferido(ANCHO_CAMPO);

        JPanel filaPassword = new JPanel(new BorderLayout());
        filaPassword.setOpaque(false);
        filaPassword.add(lblPassword, BorderLayout.WEST);
        filaPassword.add(new Enlace("¿Olvidó su clave?", this::recordarClave), BorderLayout.EAST);

        btnIngresar = new BotonRedondeado("Ingresar", BotonRedondeado.Estilo.PRIMARIO);
        btnRegistro = new BotonRedondeado("Registro nuevo", BotonRedondeado.Estilo.SECUNDARIO);
        btnSalir = new BotonRedondeado("Salir", BotonRedondeado.Estilo.TEXTO);

        anchoCompleto(lblTitulo);
        anchoCompleto(lblSubtitulo);
        anchoCompleto(lblUsuario);
        anchoCompleto(txtUsuario);
        anchoCompleto(filaPassword);
        anchoCompleto(txtPassword);
        anchoCompleto(btnIngresar);
        anchoCompleto(btnRegistro);
        anchoCompleto(btnSalir);

        contenido.add(lblTitulo);
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(lblSubtitulo);
        contenido.add(Box.createVerticalStrut(34));
        contenido.add(lblUsuario);
        contenido.add(Box.createVerticalStrut(7));
        contenido.add(txtUsuario);
        contenido.add(Box.createVerticalStrut(16));
        contenido.add(filaPassword);
        contenido.add(Box.createVerticalStrut(7));
        contenido.add(txtPassword);
        contenido.add(Box.createVerticalStrut(26));
        contenido.add(btnIngresar);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(btnRegistro);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(btnSalir);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        centro.add(contenido);
        panel.add(centro, BorderLayout.CENTER);

        JLabel pie = new JLabel("SIGELAB v2.4   ·   Departamento de Laboratorios 2026", SwingConstants.CENTER);
        pie.setFont(Tema.pie());
        pie.setForeground(Tema.PIE);
        pie.setBorder(new EmptyBorder(0, 0, 18, 0));
        panel.add(pie, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel etiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(Tema.etiqueta());
        etiqueta.setForeground(Tema.ETIQUETA);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        return etiqueta;
    }

    /** Hace que un componente ocupe todo el ancho disponible dentro del panel. */
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

        SwingUtilities.invokeLater(() -> loginForm.txtUsuario.requestFocusInWindow());

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

    private void recordarClave() {
        Dialogos.informar(panelPrincipal,
                "La recuperación de contraseña la gestiona el Departamento de Laboratorios.\n"
                        + "Comuníquese con el administrador del sistema.",
                "Recuperar contraseña");
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
