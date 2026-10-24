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
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginForm {

    private static final int ANCHO_PANEL = 520;
    private static final int ALTO_PANEL = 640;

    private JPanel panelPrincipal;
    private CampoTexto txtUsuario;
    private CampoPassword txtPassword;
    private BotonRedondeado btnIngresar;
    private BotonRedondeado btnRegistro;
    private JLabel lblSalir;

    // Control maestro de animación sincronizada al unísono
    private static float alphaGlobal = 0.3f;
    private static float deltaGlobal = 0.02f;
    private Timer timerSincronizado;

    private final UsuarioController usuarioController;

    public LoginForm() {
        this(new UsuarioController());
    }

    public LoginForm(UsuarioController usuarioController) {
        this.usuarioController = usuarioController;
        construirUI();
        configurarEventos();
    }

    private void construirUI() {
        panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(Tema.FONDO);
        panelPrincipal.setPreferredSize(new Dimension(ANCHO_PANEL + 480, ALTO_PANEL));

        JPanel pForm = crearPanelFormulario();
        PanelIlustracion pIlustracion = new PanelIlustracion();

        panelPrincipal.add(pForm, BorderLayout.WEST);
        panelPrincipal.add(pIlustracion, BorderLayout.CENTER);

        timerSincronizado = new Timer(20, e -> {
            alphaGlobal += deltaGlobal;
            if (alphaGlobal >= 1.0f) {
                alphaGlobal = 1.0f;
                deltaGlobal = -deltaGlobal;
            } else if (alphaGlobal <= 0.25f) {
                alphaGlobal = 0.25f;
                deltaGlobal = -deltaGlobal;
            }
            pForm.repaint();
            pIlustracion.repaint();
        });
        timerSincronizado.start();
    }

    private JPanel crearPanelFormulario() {
        Color fondoSeccionLogo = new Color(0x0A, 0x12, 0x24);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(fondoSeccionLogo);
        panel.setPreferredSize(new Dimension(ANCHO_PANEL, ALTO_PANEL));

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(new EmptyBorder(0, 35, 0, 35));

        // 1. Tag "PORTAL DE AUTENTICACIÓN" Sincronizado
        JPanel panelHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelHeader.setOpaque(false);

        JPanel pill = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int aVal = (int) (255 * alphaGlobal);
                g2.setColor(new Color(0x00, 0xD8, 0xF6, Math.min(255, Math.max(0, aVal))));
                g2.fillRoundRect(0, 3, 24, 8, 6, 6);
                g2.dispose();
            }
        };
        pill.setPreferredSize(new Dimension(28, 14));
        pill.setOpaque(false);

        JLabel lblPortal = new JLabel("PORTAL DE AUTENTICACIÓN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int aVal = (int) (255 * alphaGlobal);
                g2.setColor(new Color(0x00, 0xD8, 0xF6, Math.min(255, Math.max(0, aVal))));
                g2.setFont(getFont());
                g2.drawString(getText(), 0, g2.getFontMetrics().getAscent());
                g2.dispose();
            }
        };
        lblPortal.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblPortal.setPreferredSize(new Dimension(200, 14));

        panelHeader.add(pill);
        panelHeader.add(lblPortal);

        // 2. SIGELAB + Icono de Nube Neón Sincronizado
        JPanel panelLogo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelLogo.setOpaque(false);

        JLabel lblTitulo = new JLabel("SIGELAB");
        lblTitulo.setFont(Tema.titulo());
        lblTitulo.setForeground(Color.WHITE);

        JPanel boxIcono = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int aVal = (int) (255 * alphaGlobal);
                Color cNeon = new Color(0x00, 0xD8, 0xF6, Math.min(255, Math.max(0, aVal)));

                for (int i = 4; i > 0; i--) {
                    g2.setColor(new Color(0x00, 0xD8, 0xF6, (int) ((20 / i) * alphaGlobal)));
                    g2.fillRoundRect(-i, -i, getWidth() + (i * 2), getHeight() + (i * 2), 10 + i, 10 + i);
                }

                g2.setColor(new Color(0x0A, 0x1C, 0x30));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(cNeon);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                // DIBUJO VECTORIAL DE NUBE
                int cx = getWidth() / 2;
                int cy = getHeight() / 2 + 1;

                g2.drawArc(cx - 10, cy - 4, 10, 8, 90, 180);
                g2.drawArc(cx - 6, cy - 8, 10, 10, 30, 160);
                g2.drawArc(cx + 1, cy - 6, 8, 8, -40, 160);
                g2.drawLine(cx - 10, cy + 4, cx + 8, cy + 4);

                g2.dispose();
            }
        };
        boxIcono.setPreferredSize(new Dimension(34, 34));
        boxIcono.setOpaque(false);

        panelLogo.add(lblTitulo);
        panelLogo.add(boxIcono);

        JLabel lblSubtitulo = new JLabel("Sistema de Gestión y Control de Laboratorios Universitarios");
        lblSubtitulo.setFont(Tema.subtitulo());
        lblSubtitulo.setForeground(Tema.TEXTO_SUAVE);

        // 3. Chip Ubicación
        JPanel panelUbicacion = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x06, 0x0C, 0x18));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(Tema.BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        panelUbicacion.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 4));
        panelUbicacion.setOpaque(false);

        JPanel iconEdificio = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Tema.ETIQUETA);
                g2.drawRect(2, 4, 10, 10);
                g2.drawRect(5, 8, 4, 6);
                g2.drawLine(1, 4, 7, 0);
                g2.drawLine(7, 0, 13, 4);
                g2.dispose();
            }
        };
        iconEdificio.setPreferredSize(new Dimension(15, 15));
        iconEdificio.setOpaque(false);

        JLabel lblUbicacionTexto = new JLabel("Ubicación de Acceso:");
        lblUbicacionTexto.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblUbicacionTexto.setForeground(Tema.TEXTO_SUAVE);

        JPanel tagUni = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x0C, 0x22, 0x3D));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);

                int aVal = (int) (255 * alphaGlobal);
                g2.setColor(new Color(0x18, 0x3A, 0x60, Math.min(255, Math.max(0, aVal))));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
            }
        };
        tagUni.setOpaque(false);
        tagUni.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 2));

        JLabel lblUni = new JLabel("UNIVERSIDAD MARIANO GÁLVEZ DE GUATEMALA") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int aVal = (int) (255 * alphaGlobal);
                g2.setColor(new Color(0x00, 0xD8, 0xF6, Math.min(255, Math.max(0, aVal))));
                g2.setFont(getFont());
                g2.drawString(getText(), 0, g2.getFontMetrics().getAscent());
                g2.dispose();
            }
        };
        lblUni.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblUni.setPreferredSize(new Dimension(245, 12));

        tagUni.add(lblUni);

        panelUbicacion.add(iconEdificio);
        panelUbicacion.add(lblUbicacionTexto);
        panelUbicacion.add(tagUni);

        // 4. Campos de texto
        JLabel lblUsuario = etiqueta("NOMBRE DE USUARIO O CÓDIGO");
        txtUsuario = new CampoTexto(18);
        txtUsuario.setPlaceholder("Ej. 0901-22-14022 o usuario");

        JPanel filaPassword = new JPanel(new BorderLayout());
        filaPassword.setOpaque(false);
        JLabel lblPassword = etiqueta("CONTRASEÑA");
        filaPassword.add(lblPassword, BorderLayout.WEST);
        filaPassword.add(new Enlace("¿Olvidó su clave?", this::recordarClave), BorderLayout.EAST);

        txtPassword = new CampoPassword(18);
        txtPassword.setPlaceholder("••••••••••••");

        // 5. Botones
        btnIngresar = new BotonRedondeado("Ingresar al Sistema", BotonRedondeado.Estilo.PRIMARIO);
        btnRegistro = new BotonRedondeado("Registro de Estudiante / Nuevo Usuario", BotonRedondeado.Estilo.SECUNDARIO);

        lblSalir = new JLabel("Salir de la plataforma", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getForeground());
                int x = (getWidth() - g2.getFontMetrics().stringWidth(getText())) / 2 - 16;
                int y = getHeight() / 2 - 4;
                g2.drawRect(x, y, 7, 9);
                g2.drawLine(x + 4, y + 4, x + 10, y + 4);
                g2.drawLine(x + 8, y + 2, x + 10, y + 4);
                g2.drawLine(x + 8, y + 6, x + 10, y + 4);
                g2.dispose();
            }
        };
        lblSalir.setFont(Tema.enlace());
        lblSalir.setForeground(Tema.TEXTO_SUAVE);
        lblSalir.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblSalir.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                lblSalir.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                lblSalir.setForeground(Tema.TEXTO_SUAVE);
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }
        });

        anchoCompleto(panelHeader);
        anchoCompleto(panelLogo);
        anchoCompleto(lblSubtitulo);
        anchoCompleto(panelUbicacion);
        anchoCompleto(lblUsuario);
        anchoCompleto(txtUsuario);
        anchoCompleto(filaPassword);
        anchoCompleto(txtPassword);
        anchoCompleto(btnIngresar);
        anchoCompleto(btnRegistro);
        anchoCompleto(lblSalir);

        contenido.add(panelHeader);
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(panelLogo);
        contenido.add(Box.createVerticalStrut(4));
        contenido.add(lblSubtitulo);
        contenido.add(Box.createVerticalStrut(16));
        contenido.add(panelUbicacion);
        contenido.add(Box.createVerticalStrut(18));
        contenido.add(lblUsuario);
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(txtUsuario);
        contenido.add(Box.createVerticalStrut(12));
        contenido.add(filaPassword);
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(txtPassword);
        contenido.add(Box.createVerticalStrut(20));
        contenido.add(btnIngresar);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(btnRegistro);
        contenido.add(Box.createVerticalStrut(18));
        contenido.add(lblSalir);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        centro.add(contenido);
        panel.add(centro, BorderLayout.CENTER);

        return panel;
    }

    public static float getAlphaGlobal() {
        return alphaGlobal;
    }

    private JLabel etiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(Tema.etiqueta());
        etiqueta.setForeground(Tema.TEXTO_SUAVE);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        return etiqueta;
    }

    private void anchoCompleto(JComponent componente) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension preferido = componente.getPreferredSize();
        componente.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferido.height));
    }

    public static void mostrar() {
        LoginForm loginForm = new LoginForm();

        JFrame frame = new JFrame("SIGELAB - Inicio de sesión");
        frame.setContentPane(loginForm.getPanelPrincipal());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        frame.pack();
        frame.setLocationRelativeTo(null);

        frame.getRootPane().setDefaultButton(loginForm.btnIngresar);

        SwingUtilities.invokeLater(() -> loginForm.txtUsuario.requestFocusInWindow());

        Animaciones.mostrarConDesvanecido(frame);
    }

    private void configurarEventos() {
        btnIngresar.addActionListener(e -> ingresar());

        btnRegistro.addActionListener(e -> {
            btnRegistro.destello();
            Window duenio = SwingUtilities.getWindowAncestor(panelPrincipal);
            RegistroUsuarioForm.abrir(duenio, usuarioController);
        });
    }

    private void ingresar() {
        Window ventana = SwingUtilities.getWindowAncestor(panelPrincipal);
        try {
            Usuario usuario = usuarioController.iniciarSesion(
                    txtUsuario.getText().trim(),
                    new String(txtPassword.getPassword()),
                    (autenticado, rostroRegistrado) ->
                            DialogoValidacionFacial.aprobar(ventana, autenticado, rostroRegistrado));

            ofrecerRegistroRostro(ventana, usuario);
            MainForm.mostrar(usuario);
            cerrarVentana();
        } catch (Exception e) {
            txtPassword.setText("");
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void ofrecerRegistroRostro(Window ventana, Usuario usuario) {
        if (usuarioController.tieneRostroRegistrado(usuario.getIdUsuario())) return;
        if (Dialogos.confirmar(ventana,
                "Su cuenta todavía no tiene un rostro registrado.\n"
                        + "¿Desea registrarlo ahora para usarlo como segundo factor de acceso?",
                "Registro facial")) {
            DialogoRegistroRostro.abrir(ventana, usuarioController, usuario);
        }
    }

    private void recordarClave() {
        Window duenio = SwingUtilities.getWindowAncestor(panelPrincipal);
        DialogoRecuperacion.abrir(duenio, usuarioController);
    }

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