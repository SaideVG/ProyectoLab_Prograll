package universidad.sigelab.view;

import universidad.sigelab.controller.UsuarioController;
import universidad.sigelab.enums.EstadoRegistro;
import universidad.sigelab.model.Rol;
import universidad.sigelab.model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Optional;

public class UsuarioView {

    // Componentes vinculados desde UsuarioView.form
    private JPanel panelPrincipal;

    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JTextField txtCorreo;
    private JComboBox<Rol> cboRol;

    private JCheckBox chkActivo;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnActivarDesactivar;

    private JTextField txtBuscar;
    private JButton btnBuscar;

    private JTable tblUsuarios;
    private JButton btnSalir;

    // Componente visual extra para el conteo
    private JLabel lblConteoUsuarios;

    // Controlador y Usuario actual
    private Usuario usuarioActual;
    private final UsuarioController usuarioController;

    public UsuarioView() {
        this(null, new UsuarioController());
    }

    public UsuarioView(Usuario usuario) {
        this(usuario, new UsuarioController());
    }

    public UsuarioView(UsuarioController usuarioController) {
        this(null, usuarioController);
    }

    public UsuarioView(Usuario usuario, UsuarioController usuarioController) {
        this.usuarioActual = usuario;
        this.usuarioController = usuarioController;

        construirInterfazCohesiva();
        configurarFormulario();
        configurarEventos();
        cargarRoles();
        cargarUsuarios();
    }

    private void construirInterfazCohesiva() {
        panelPrincipal = new JPanel(new BorderLayout(0, 14));
        panelPrincipal.setBackground(new Color(11, 15, 25));
        panelPrincipal.setBorder(new EmptyBorder(16, 20, 16, 20));

        panelPrincipal.add(crearHeaderGlobal(), BorderLayout.NORTH);

        JPanel centroPanel = new JPanel();
        centroPanel.setLayout(new BoxLayout(centroPanel, BoxLayout.Y_AXIS));
        centroPanel.setOpaque(false);

        JPanel cardFormulario = crearCardFormulario();
        JPanel cardTabla = crearCardTabla();

        cardFormulario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        centroPanel.add(cardFormulario);
        centroPanel.add(Box.createVerticalStrut(12));
        centroPanel.add(cardTabla);

        panelPrincipal.add(centroPanel, BorderLayout.CENTER);
        panelPrincipal.add(crearFooterGlobal(), BorderLayout.SOUTH);
    }

    private JPanel crearHeaderGlobal() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel leftContainer = new JPanel(new BorderLayout(12, 0));
        leftContainer.setOpaque(false);

        // Barra indicadora rosa/magenta (#EC4899)
        JPanel barraIndicadora = new JPanel();
        barraIndicadora.setBackground(new Color(236, 72, 153));
        barraIndicadora.setPreferredSize(new Dimension(4, 40));

        JPanel titleAndLogoBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleAndLogoBox.setOpaque(false);

        JLabel lblTitle = new JLabel("ADMINISTRACIÓN DE USUARIOS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JPanel iconCirclePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int d = Math.min(getWidth(), getHeight()) - 4;
                int x = (getWidth() - d) / 2;
                int y = (getHeight() - d) / 2;

                g2.setColor(new Color(236, 72, 153, 40));
                g2.fillOval(x, y, d, d);

                g2.setColor(new Color(236, 72, 153));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, d, d);

                g2.dispose();
            }
        };
        iconCirclePanel.setPreferredSize(new Dimension(32, 32));
        iconCirclePanel.setOpaque(false);
        iconCirclePanel.setLayout(new GridBagLayout());

        JLabel lblIconUser = new JLabel("👤");
        lblIconUser.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        lblIconUser.setForeground(new Color(236, 72, 153));
        iconCirclePanel.add(lblIconUser);

        titleAndLogoBox.add(lblTitle);
        titleAndLogoBox.add(iconCirclePanel);

        JLabel lblSub = new JLabel("Control de acceso, gestión de perfiles y asignación de roles");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184));

        JPanel titleSubBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleSubBox.setOpaque(false);
        titleSubBox.add(titleAndLogoBox);
        titleSubBox.add(lblSub);

        leftContainer.add(barraIndicadora, BorderLayout.WEST);
        leftContainer.add(titleSubBox, BorderLayout.CENTER);

        header.add(leftContainer, BorderLayout.WEST);
        header.add(crearBadgeUsuarioDinamico(usuarioActual), BorderLayout.EAST);

        return header;
    }

    private JPanel crearBadgeUsuarioDinamico(Usuario usuario) {
        JPanel infoBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        infoBox.setOpaque(false);

        String nombreUser = (usuario != null && usuario.getUsuario() != null) ? usuario.getUsuario() : "admin";
        String iconoUser = obtenerIconoUser(nombreUser);

        String rolUser = "ADMIN";
        if (usuario != null && usuario.getRol() != null && usuario.getRol().getNombre() != null) {
            rolUser = usuario.getRol().getNombre().name();
        } else {
            String uLower = nombreUser.toLowerCase();
            if (uLower.contains("encargado")) rolUser = "ENCARGADO";
            else if (uLower.contains("docente")) rolUser = "DOCENTE";
            else if (uLower.contains("inactivo")) rolUser = "INACTIVO";
        }

        JLabel lblOperador = new JLabel(iconoUser + " " + nombreUser);
        lblOperador.setFont(new Font("Segoe UI Symbol", Font.BOLD, 13));
        lblOperador.setForeground(Color.WHITE);

        final String rolFinal = rolUser;
        final String nombreFinal = nombreUser;

        JPanel rolChipPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(obtenerColorRolHeader(rolFinal, nombreFinal));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
            }
        };
        rolChipPanel.setOpaque(false);

        JLabel lblRolTag = new JLabel(rolUser.toUpperCase());
        lblRolTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblRolTag.setForeground(Color.WHITE);
        rolChipPanel.add(lblRolTag);

        infoBox.add(lblOperador);
        infoBox.add(rolChipPanel);

        return infoBox;
    }

    private String obtenerIconoUser(String usuario) {
        if (usuario == null) return "👑";
        String u = usuario.toLowerCase();

        if (u.contains("admin")) return "👑";
        if (u.contains("encargado")) return "💼";
        if (u.contains("docente")) return "🎓";
        if (u.contains("inactivo")) return "🚫";

        return "👤";
    }

    private Color obtenerColorRolHeader(String rol, String nombreUsuario) {
        if (nombreUsuario != null && nombreUsuario.toLowerCase().contains("inactivo")) {
            return new Color(239, 68, 68);
        }

        if (rol == null) return new Color(30, 58, 138);

        switch (rol.toUpperCase()) {
            case "ADMIN":
            case "ADMINISTRADOR":
                return new Color(30, 58, 138);
            case "ENCARGADO":
                return new Color(16, 185, 129);
            case "DOCENTE":
                return new Color(245, 158, 11);
            default:
                return new Color(239, 68, 68);
        }
    }

    private JPanel crearCardFormulario() {
        CardEstiloSistema card = new CardEstiloSistema(12, new Color(18, 24, 38));
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel topHeaderBox = new JPanel(new BorderLayout());
        topHeaderBox.setOpaque(false);

        JLabel lblTag = new JLabel(" DATOS DEL USUARIO // REGISTRO Y EDICIÓN ");
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTag.setForeground(new Color(56, 189, 248));
        lblTag.setBorder(new LineBorder(new Color(14, 116, 144), 1, true));

        JLabel lblEstadoTop = new JLabel("☑ ACTIVO");
        lblEstadoTop.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        lblEstadoTop.setForeground(new Color(52, 211, 153));

        topHeaderBox.add(lblTag, BorderLayout.WEST);
        topHeaderBox.add(lblEstadoTop, BorderLayout.EAST);

        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setOpaque(false);

        JPanel grid3x2 = new JPanel(new GridLayout(3, 2, 16, 8));
        grid3x2.setOpaque(false);

        if (txtId == null) txtId = new JTextField();
        if (txtNombre == null) txtNombre = new JTextField();
        if (txtApellido == null) txtApellido = new JTextField();
        if (txtUsuario == null) txtUsuario = new JTextField();
        if (txtPassword == null) txtPassword = new JPasswordField();
        if (txtCorreo == null) txtCorreo = new JTextField();
        if (cboRol == null) cboRol = new JComboBox<>();
        if (chkActivo == null) chkActivo = new JCheckBox("Activo");

        estilarCampoTexto(txtId);
        estilarCampoTexto(txtNombre);
        estilarCampoTexto(txtApellido);
        estilarCampoTexto(txtUsuario);
        estilarCampoTexto(txtPassword);
        estilarCampoTexto(txtCorreo);
        estilarComboBox(cboRol);

        grid3x2.add(crearCampo("🆔 ID", txtId));
        grid3x2.add(crearCampo("👤 NOMBRE *", txtNombre));
        grid3x2.add(crearCampo("👥 APELLIDO *", txtApellido));
        grid3x2.add(crearCampo("👤 USUARIO *", txtUsuario));
        grid3x2.add(crearCampo("🔐 CONTRASEÑA", txtPassword));
        grid3x2.add(crearCampo("📧 CORREO", txtCorreo));

        JPanel panelRolCentrado = new JPanel(new BorderLayout());
        panelRolCentrado.setOpaque(false);
        panelRolCentrado.setBorder(new EmptyBorder(6, 0, 0, 0));
        panelRolCentrado.add(crearCampo("🪪 ROL *", cboRol), BorderLayout.CENTER);

        formContainer.add(grid3x2);
        formContainer.add(panelRolCentrado);

        JPanel botoneraForm = new JPanel(new GridLayout(1, 4, 12, 0));
        botoneraForm.setOpaque(false);

        btnNuevo = crearBotonOutlined("+ NUEVO", new Color(15, 23, 42), new Color(56, 189, 248), new Color(30, 58, 138));
        btnGuardar = crearBotonSolidoDestacado("🔒 GUARDAR", Color.WHITE, new Color(15, 23, 42));
        btnActualizar = crearBotonOutlined("↻ ACTUALIZAR", new Color(15, 23, 42), new Color(148, 163, 184), new Color(51, 65, 85));
        btnActivarDesactivar = crearBotonOutlinedRojo("🚫 DESACTIVAR", new Color(15, 23, 42), new Color(248, 113, 113), new Color(153, 27, 27));

        botoneraForm.add(btnNuevo);
        botoneraForm.add(btnGuardar);
        botoneraForm.add(btnActualizar);
        botoneraForm.add(btnActivarDesactivar);

        card.add(topHeaderBox, BorderLayout.NORTH);
        card.add(formContainer, BorderLayout.CENTER);
        card.add(botoneraForm, BorderLayout.SOUTH);

        return card;
    }

    private JPanel crearCardTabla() {
        CardEstiloSistema card = new CardEstiloSistema(12, new Color(18, 24, 38));
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setOpaque(false);

        JLabel lblSearch = new JLabel("🔍 Buscar por usuario");
        lblSearch.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        lblSearch.setForeground(Color.WHITE);

        if (txtBuscar == null) txtBuscar = new JTextField();
        estilarCampoTexto(txtBuscar);

        btnBuscar = crearBotonOutlined("🔍 BUSCAR", new Color(30, 41, 59), Color.WHITE, new Color(51, 65, 85));
        btnBuscar.setPreferredSize(new Dimension(110, 34));

        searchBar.add(lblSearch, BorderLayout.WEST);
        searchBar.add(txtBuscar, BorderLayout.CENTER);
        searchBar.add(btnBuscar, BorderLayout.EAST);

        if (tblUsuarios == null) tblUsuarios = new JTable();
        JScrollPane scroll = new JScrollPane(tblUsuarios);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(18, 24, 38));

        JPanel corner = new JPanel();
        corner.setBackground(new Color(18, 24, 38));
        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, corner);

        estilarScroll(scroll.getVerticalScrollBar());

        card.add(searchBar, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel crearFooterGlobal() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        lblConteoUsuarios = new JLabel("● Mostrando 0 usuarios registrados en el sistema");
        lblConteoUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblConteoUsuarios.setForeground(new Color(148, 163, 184));

        btnSalir = crearBotonOutlined("✖ SALIR", new Color(30, 41, 59), Color.WHITE, new Color(51, 65, 85));
        btnSalir.setPreferredSize(new Dimension(110, 36));

        footer.add(lblConteoUsuarios, BorderLayout.WEST);
        footer.add(btnSalir, BorderLayout.EAST);

        return footer;
    }

    private JPanel crearCampo(String titulo, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);

        JLabel l = new JLabel(titulo);
        l.setFont(new Font("Segoe UI Emoji", Font.BOLD, 10));
        l.setForeground(new Color(148, 163, 184));

        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void estilarCampoTexto(JTextField tf) {
        if (tf == null) return;
        tf.setBackground(new Color(15, 23, 42));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        LineBorder borderNormal = new LineBorder(new Color(46, 59, 82), 1, true);
        LineBorder borderFocus = new LineBorder(new Color(59, 130, 246), 2, true);

        tf.setBorder(BorderFactory.createCompoundBorder(borderNormal, new EmptyBorder(4, 8, 4, 8)));

        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(borderFocus, new EmptyBorder(3, 7, 3, 7)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(borderNormal, new EmptyBorder(4, 8, 4, 8)));
            }
        });
    }

    private void estilarComboBox(JComboBox<?> cbo) {
        if (cbo == null) return;
        cbo.setBackground(new Color(15, 23, 42));
        cbo.setForeground(Color.WHITE);
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbo.setBorder(new LineBorder(new Color(46, 59, 82), 1, true));

        cbo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("▼");
                btn.setFont(new Font("Segoe UI", Font.BOLD, 8));
                btn.setBackground(new Color(30, 41, 59));
                btn.setForeground(new Color(148, 163, 184));
                btn.setFocusPainted(false);
                btn.setBorder(BorderFactory.createLineBorder(new Color(46, 59, 82)));
                return btn;
            }
        });
    }

    private JButton crearBotonOutlined(String texto, Color bg, Color fgText, Color borderColor) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(fgText);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        return btn;
    }

    private JButton crearBotonSolidoDestacado(String texto, Color bg, Color fgText) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fgText);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.WHITE, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        return btn;
    }

    private JButton crearBotonOutlinedRojo(String texto, Color bg, Color fgText, Color borderColor) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(fgText);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        return btn;
    }

    private static class CardEstiloSistema extends JPanel {
        private final int cornerRadius;
        private final Color bgColor;

        public CardEstiloSistema(int radius, Color bg) {
            this.cornerRadius = radius;
            this.bgColor = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

            g2.setColor(new Color(46, 59, 82));
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius);

            g2.dispose();
        }
    }

    private void estilarScroll(JScrollBar sb) {
        if (sb == null) return;
        sb.setBackground(new Color(18, 24, 38));
        sb.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(51, 65, 85);
                this.trackColor = new Color(18, 24, 38);
            }

            @Override protected JButton createDecreaseButton(int o) { return b(); }
            @Override protected JButton createIncreaseButton(int o) { return b(); }
            private JButton b() { JButton btn = new JButton(); btn.setPreferredSize(new Dimension(0,0)); return btn; }
        });
    }

    private void configurarFormulario() {
        txtId.setEditable(false);
        chkActivo.setEnabled(false);

        txtPassword.setToolTipText("Al actualizar, déjela vacía para conservar la contraseña actual.");

        tblUsuarios.setModel(new TablaSoloLectura("ID", "USUARIO", "NOMBRE", "APELLIDO", "CORREO", "ROL", "ESTADO"));
        tblUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configurarEstiloTabla();

        limpiarFormulario();
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnActivarDesactivar.addActionListener(e -> cambiarEstado());
        btnBuscar.addActionListener(e -> buscar());
        btnSalir.addActionListener(e -> salir());

        if (txtBuscar != null) {
            txtBuscar.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        buscar();
                    }
                }
            });
        }

        tblUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarUsuario();
        });
    }

    private void cargarRoles() {
        try {
            cboRol.removeAllItems();
            for (Rol rol : usuarioController.listarRoles()) cboRol.addItem(rol);
            cboRol.setSelectedIndex(-1);
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void guardar() {
        try {
            Usuario usuario = new Usuario(txtNombre.getText(), txtApellido.getText(), txtUsuario.getText(),
                    null, txtCorreo.getText(), (Rol) cboRol.getSelectedItem());

            if (usuarioController.guardar(usuario, leerPassword())) {
                Dialogos.informar(panelPrincipal, "Usuario guardado correctamente.", "Usuario");
                limpiarFormulario();
                cargarUsuarios();
            } else {
                Dialogos.error(panelPrincipal, "No se pudo guardar el usuario.");
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void cargarUsuarios() {
        try {
            mostrarEnTabla(usuarioController.listar());
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void mostrarEnTabla(List<Usuario> usuarios) {
        DefaultTableModel modelo = (DefaultTableModel) tblUsuarios.getModel();
        modelo.setRowCount(0);

        for (Usuario usuario : usuarios) {
            modelo.addRow(new Object[]{
                    usuario.getIdUsuario(),
                    usuario.getUsuario(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getCorreo(),
                    usuario.getRol() != null ? usuario.getRol().getNombre().name() : "-",
                    usuario.isActivo() ? "ACTIVO" : "INACTIVO"
            });
        }

        if (lblConteoUsuarios != null) {
            lblConteoUsuarios.setText("● Mostrando " + usuarios.size() + " usuarios registrados en el sistema");
        }
    }

    private void actualizar() {
        if (txtId.getText().isBlank()) {
            Dialogos.advertir(panelPrincipal, "Debe seleccionar un usuario.");
            return;
        }

        try {
            Usuario usuario = new Usuario(Integer.parseInt(txtId.getText()), txtNombre.getText(),
                    txtApellido.getText(), txtUsuario.getText(), null, txtCorreo.getText(),
                    (Rol) cboRol.getSelectedItem(), null, null);

            if (usuarioController.actualizar(usuario, leerPassword())) {
                Dialogos.informar(panelPrincipal, "Usuario actualizado correctamente.", "Usuario");
                limpiarFormulario();
                cargarUsuarios();
            } else {
                Dialogos.error(panelPrincipal, "No se pudo actualizar el usuario.");
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void cambiarEstado() {
        if (txtId.getText().isBlank()) {
            Dialogos.advertir(panelPrincipal, "Debe seleccionar un usuario.");
            return;
        }

        boolean activar = !chkActivo.isSelected();
        String accion = activar ? "activar" : "desactivar";
        if (!Dialogos.confirmar(panelPrincipal, "¿Está seguro de " + accion + " al usuario?", "Confirmar")) return;

        try {
            int idUsuario = Integer.parseInt(txtId.getText());
            EstadoRegistro nuevoEstado = activar ? EstadoRegistro.ACTIVO : EstadoRegistro.INACTIVO;

            if (usuarioController.cambiarEstado(idUsuario, nuevoEstado)) {
                Dialogos.informar(panelPrincipal,
                        activar ? "Usuario activado correctamente." : "Usuario desactivado correctamente.", "Usuario");
                cargarUsuarios();
                seleccionarFilaPorId(idUsuario);
            } else {
                Dialogos.error(panelPrincipal, "No se pudo cambiar el estado del usuario.");
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void buscar() {
        String nombreUsuario = txtBuscar.getText().trim();

        if (nombreUsuario.isBlank()) {
            limpiarFormulario();
            cargarUsuarios();
            return;
        }

        try {
            Optional<Usuario> resultado = usuarioController.buscarPorUsuario(nombreUsuario);

            if (resultado.isPresent()) {
                mostrarEnTabla(List.of(resultado.get()));
                seleccionarFilaPorId(resultado.get().getIdUsuario());
            } else {
                Dialogos.informar(panelPrincipal, "No se encontró ningún usuario con ese nombre.", "Búsqueda");
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void seleccionarUsuario() {
        int fila = tblUsuarios.getSelectedRow();
        if (fila == -1) return;

        try {
            int idUsuario = (int) tblUsuarios.getValueAt(fila, 0);
            usuarioController.buscar(idUsuario).ifPresent(this::mostrarEnFormulario);
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void mostrarEnFormulario(Usuario usuario) {
        txtId.setText(String.valueOf(usuario.getIdUsuario()));
        txtNombre.setText(usuario.getNombre());
        txtApellido.setText(usuario.getApellido());
        txtUsuario.setText(usuario.getUsuario());
        txtCorreo.setText(usuario.getCorreo());
        txtPassword.setText("");
        cboRol.setSelectedItem(usuario.getRol());
        chkActivo.setSelected(usuario.isActivo());

        btnGuardar.setEnabled(false);
        btnActualizar.setEnabled(true);
        btnActivarDesactivar.setEnabled(true);
        btnActivarDesactivar.setText(usuario.isActivo() ? "🚫 DESACTIVAR" : "✓ ACTIVAR");
    }

    private void seleccionarFilaPorId(int idUsuario) {
        for (int fila = 0; fila < tblUsuarios.getRowCount(); fila++) {
            if ((int) tblUsuarios.getValueAt(fila, 0) == idUsuario) {
                tblUsuarios.setRowSelectionInterval(fila, fila);
                tblUsuarios.scrollRectToVisible(tblUsuarios.getCellRect(fila, 0, true));
                return;
            }
        }
    }

    private void nuevo() {
        limpiarFormulario();
        txtNombre.requestFocus();
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtUsuario.setText("");
        txtPassword.setText("");
        txtCorreo.setText("");
        txtBuscar.setText("");
        cboRol.setSelectedIndex(-1);

        chkActivo.setSelected(true);
        tblUsuarios.clearSelection();

        btnGuardar.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnActivarDesactivar.setEnabled(false);
        btnActivarDesactivar.setText("🚫 DESACTIVAR");
    }

    private String leerPassword() {
        return new String(txtPassword.getPassword());
    }

    private void salir() {
        Window win = SwingUtilities.getWindowAncestor(panelPrincipal);
        if (win != null) {
            win.dispose();
        }
    }

    private void configurarEstiloTabla() {
        if (tblUsuarios == null) return;

        tblUsuarios.setBackground(new Color(18, 24, 38));
        tblUsuarios.setForeground(new Color(226, 232, 240));
        tblUsuarios.setSelectionBackground(new Color(30, 58, 138));
        tblUsuarios.setSelectionForeground(Color.WHITE);
        tblUsuarios.setRowHeight(38);
        tblUsuarios.setShowGrid(false);

        if (tblUsuarios.getTableHeader() != null) {
            tblUsuarios.getTableHeader().setBackground(new Color(11, 15, 25));
            tblUsuarios.getTableHeader().setForeground(new Color(148, 163, 184));
            tblUsuarios.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
            tblUsuarios.getTableHeader().setPreferredSize(new Dimension(0, 32));
        }

        DefaultTableCellRenderer renderRolChip = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setOpaque(false);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 10));

                String rolStr = value != null ? value.toString().toUpperCase() : "-";
                label.setText(rolStr);

                Color bgChip = new Color(30, 58, 138);
                Color fgChip = new Color(147, 197, 253);

                if (rolStr.contains("DOCENTE")) {
                    bgChip = new Color(12, 74, 110);
                    fgChip = new Color(56, 189, 248);
                } else if (rolStr.contains("ENCARGADO")) {
                    bgChip = new Color(120, 53, 15);
                    fgChip = new Color(251, 191, 36);
                }

                Color finalBgChip = bgChip;
                Color finalFgChip = fgChip;

                label.setForeground(finalFgChip);

                JPanel panel = new JPanel(new BorderLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        int w = getWidth() - 20;
                        int h = getHeight() - 16;
                        int x = (getWidth() - w) / 2;
                        int y = (getHeight() - h) / 2;

                        g2.setColor(finalBgChip);
                        g2.fillRoundRect(x, y, w, h, 10, 10);
                        g2.dispose();
                    }
                };

                panel.setOpaque(true);
                panel.setBackground(isSelected ? new Color(30, 41, 59) : (row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31)));
                panel.add(label, BorderLayout.CENTER);
                return panel;
            }
        };

        DefaultTableCellRenderer renderEstadoChip = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setOpaque(false);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 10));

                boolean esActivo = value != null && value.toString().equalsIgnoreCase("ACTIVO");
                label.setText(esActivo ? "● ACTIVO" : "● INACTIVO");
                label.setForeground(esActivo ? new Color(52, 211, 153) : new Color(248, 113, 113));

                JPanel panel = new JPanel(new BorderLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        int w = getWidth() - 20;
                        int h = getHeight() - 16;
                        int x = (getWidth() - w) / 2;
                        int y = (getHeight() - h) / 2;

                        g2.setColor(esActivo ? new Color(6, 78, 59, 180) : new Color(127, 29, 29, 180));
                        g2.fillRoundRect(x, y, w, h, 14, 14);

                        g2.setColor(esActivo ? new Color(16, 185, 129, 200) : new Color(239, 68, 68, 200));
                        g2.drawRoundRect(x, y, w - 1, h - 1, 14, 14);

                        g2.dispose();
                    }
                };

                panel.setOpaque(true);
                panel.setBackground(isSelected ? new Color(30, 41, 59) : (row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31)));
                panel.add(label, BorderLayout.CENTER);
                return panel;
            }
        };

        DefaultTableCellRenderer renderTexto = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setOpaque(true);
                c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                c.setHorizontalAlignment(SwingConstants.CENTER);

                if (isSelected) {
                    c.setBackground(new Color(30, 41, 59));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31));
                    c.setForeground(new Color(226, 232, 240));
                }
                return c;
            }
        };

        for (int i = 0; i < tblUsuarios.getColumnCount(); i++) {
            if (i == 5) {
                tblUsuarios.getColumnModel().getColumn(i).setCellRenderer(renderRolChip);
            } else if (i == 6) {
                tblUsuarios.getColumnModel().getColumn(i).setCellRenderer(renderEstadoChip);
            } else {
                tblUsuarios.getColumnModel().getColumn(i).setCellRenderer(renderTexto);
            }
        }
    }

    private static class TablaSoloLectura extends DefaultTableModel {
        public TablaSoloLectura(Object... columnas) {
            super(columnas, 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    public JPanel getPanelPrincipal() {
        return panelPrincipal;
    }
}