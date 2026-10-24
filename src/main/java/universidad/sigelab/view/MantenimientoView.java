package universidad.sigelab.view;

import universidad.sigelab.controller.EquipoController;
import universidad.sigelab.controller.MantenimientoController;
import universidad.sigelab.enums.ResultadoMantenimiento;
import universidad.sigelab.enums.TipoMantenimiento;
import universidad.sigelab.model.Equipo;
import universidad.sigelab.model.Mantenimiento;
import universidad.sigelab.model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MantenimientoView {

    private JPanel panelPrincipal;

    private JTextField txtCodigoEquipo;
    private JButton btnBuscarEquipo;
    private JLabel lblInfoEquipo;
    private JLabel lblEquipo;
    private JComboBox<TipoMantenimiento> cboTipo;
    private JTextField txtResponsable;
    private JTextField txtDescripcion;
    private JButton btnEnviar;

    private JCheckBox chkSoloEnProceso;
    private JTable tblMantenimientos;
    private DefaultTableModel modelTabla;
    private JLabel lblContadorRegistros;

    private JComboBox<ResultadoMantenimiento> cboResultado;
    private JTextField txtObservacionesFinales;
    private JTextField txtObservaciones;
    private JButton btnFinalizar;

    private JButton btnSalir;

    private Equipo equipoSeleccionado;
    private Usuario usuarioActual;
    private final MantenimientoController mantenimientoController;
    private final EquipoController equipoController;

    private static final Color[] PALETA_COLORES = {
            new Color(56, 189, 248),  // Cian
            new Color(16, 185, 129),  // Esmeralda
            new Color(245, 158, 11),  // Ámbar
            new Color(139, 92, 246),  // Púrpura
            new Color(236, 72, 153),  // Rosa
            new Color(20, 184, 166),  // Teal
            new Color(99, 102, 241)   // Índigo
    };

    public MantenimientoView() {
        this(null, new MantenimientoController(), new EquipoController());
    }

    public MantenimientoView(Usuario usuario) {
        this(usuario, new MantenimientoController(), new EquipoController());
    }

    public MantenimientoView(MantenimientoController mantenimientoController, EquipoController equipoController) {
        this(null, mantenimientoController, equipoController);
    }

    public MantenimientoView(Usuario usuario, MantenimientoController mantenimientoController, EquipoController equipoController) {
        this.usuarioActual = usuario;
        this.mantenimientoController = mantenimientoController;
        this.equipoController = equipoController;

        initComponents();
        configurarEventos();
        cargarTabla();
    }

    private void initComponents() {
        panelPrincipal = new JPanel(new BorderLayout(0, 12));
        panelPrincipal.setBackground(new Color(11, 15, 25));
        panelPrincipal.setBorder(new EmptyBorder(16, 20, 16, 20));

        panelPrincipal.add(crearHeader(), BorderLayout.NORTH);

        JPanel centroPanel = new JPanel();
        centroPanel.setLayout(new BoxLayout(centroPanel, BoxLayout.Y_AXIS));
        centroPanel.setOpaque(false);

        centroPanel.add(crearCardEnviar());
        centroPanel.add(Box.createVerticalStrut(10));
        centroPanel.add(crearCardTablaRegistros());
        centroPanel.add(Box.createVerticalStrut(10));
        centroPanel.add(crearCardFinalizar());

        panelPrincipal.add(centroPanel, BorderLayout.CENTER);
        panelPrincipal.add(crearFooter(), BorderLayout.SOUTH);
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(245, 158, 11));
                g2.fillRect(0, 0, 4, getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(4, 12, 4, 12));

        // Panel vertical del lado izquierdo (Título + Subtítulo)
        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        // Fila 1: Texto del título e ícono dorado ubicado a la derecha enfrente del texto
        JPanel filaTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filaTitulo.setOpaque(false);

        JLabel lblTitle = new JLabel("MANTENIMIENTO DE EQUIPOS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblIconoDorado = new JLabel(new IconoCirculoDorado());

        filaTitulo.add(lblTitle);
        filaTitulo.add(lblIconoDorado);

        // Fila 2: Subtítulo
        JLabel lblSub = new JLabel("Órdenes preventivas y correctivas");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184));
        lblSub.setBorder(new EmptyBorder(2, 0, 0, 0));

        titleBox.add(filaTitulo);
        titleBox.add(lblSub);

        // Bloque derecho: Credenciales del usuario
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightBox.setOpaque(false);
        rightBox.add(crearBadgeUsuarioDinamico(usuarioActual));

        header.add(titleBox, BorderLayout.WEST);
        header.add(rightBox, BorderLayout.EAST);

        return header;
    }

    private JPanel crearBadgeUsuarioDinamico(Usuario usuario) {
        JPanel infoBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        infoBox.setOpaque(false);

        String nombreUser = (usuario != null && usuario.getUsuario() != null) ? usuario.getUsuario() : "admin";
        String iconoUser = obtenerIcono(nombreUser);

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
                g2.setColor(obtenerColorRol(rolFinal, nombreFinal));
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

    private String obtenerIcono(String usuario) {
        if (usuario == null) return "👑";
        String u = usuario.toLowerCase();

        if (u.contains("admin")) return "👑";
        if (u.contains("encargado")) return "💼";
        if (u.contains("docente")) return "🎓";
        if (u.contains("inactivo")) return "🚫";

        return "👤";
    }

    private Color obtenerColorRol(String rol, String nombreUsuario) {
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

    private JPanel crearCardEnviar() {
        CardPanel card = new CardPanel(14, new Color(18, 24, 38));
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblTitle = new JLabel("ENVIAR EQUIPO A MANTENIMIENTO");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(56, 189, 248));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        grid.add(crearLabelCampo("Código de equipo"), gbc);

        txtCodigoEquipo = crearInput();
        gbc.gridx = 1; gbc.weightx = 0.25;
        grid.add(txtCodigoEquipo, gbc);

        btnBuscarEquipo = new BotonEstilizado("🔍 Buscar equipo", new Color(30, 58, 138), Color.WHITE, new Color(59, 130, 246, 180));
        gbc.gridx = 2; gbc.weightx = 0;
        grid.add(btnBuscarEquipo, gbc);

        lblInfoEquipo = new JLabel("Sin equipo identificado", SwingConstants.CENTER);
        lblInfoEquipo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblInfoEquipo.setForeground(new Color(148, 163, 184));
        lblInfoEquipo.setOpaque(true);
        lblInfoEquipo.setBackground(new Color(11, 15, 25));
        lblInfoEquipo.setBorder(BorderFactory.createLineBorder(new Color(46, 59, 82), 1, true));
        lblInfoEquipo.setPreferredSize(new Dimension(180, 32));

        lblEquipo = lblInfoEquipo;

        gbc.gridx = 3; gbc.weightx = 0.3;
        grid.add(lblInfoEquipo, gbc);

        gbc.gridx = 4; gbc.weightx = 0;
        grid.add(crearLabelCampo("Responsable"), gbc);

        txtResponsable = crearInput();
        gbc.gridx = 5; gbc.weightx = 0.25;
        grid.add(txtResponsable, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        grid.add(crearLabelCampo("Tipo mantenimiento"), gbc);

        cboTipo = new JComboBox<>(TipoMantenimiento.values());
        estilarComboBox(cboTipo);
        gbc.gridx = 1; gbc.weightx = 0.25;
        grid.add(cboTipo, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        grid.add(crearLabelCampo("Descripción"), gbc);

        txtDescripcion = crearInput();
        gbc.gridx = 3; gbc.gridwidth = 2; gbc.weightx = 0.5;
        grid.add(txtDescripcion, gbc);

        btnEnviar = new BotonEstilizado("⚡ ENVIAR A MANTENIMIENTO", new Color(248, 250, 252), new Color(15, 23, 42), new Color(255, 255, 255, 220));
        btnEnviar.setPreferredSize(new Dimension(200, 34));
        gbc.gridx = 5; gbc.gridwidth = 1; gbc.weightx = 0.25;
        grid.add(btnEnviar, gbc);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);

        return card;
    }

    private JPanel crearCardTablaRegistros() {
        CardPanel card = new CardPanel(14, new Color(18, 24, 38));
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        chkSoloEnProceso = new JCheckBox("Mostrar solo los mantenimientos en proceso");
        chkSoloEnProceso.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chkSoloEnProceso.setForeground(new Color(226, 232, 240));
        chkSoloEnProceso.setOpaque(false);
        chkSoloEnProceso.setSelected(true);

        lblContadorRegistros = new JLabel("REGISTROS ACTIVOS: 0 | FILTRADO: ACTIVO");
        lblContadorRegistros.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblContadorRegistros.setForeground(new Color(52, 211, 153));

        topBar.add(chkSoloEnProceso, BorderLayout.WEST);
        topBar.add(lblContadorRegistros, BorderLayout.EAST);

        tblMantenimientos = new JTable();
        configurarTabla();

        JScrollPane scroll = new JScrollPane(tblMantenimientos);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(new Color(11, 15, 25));
        scroll.getViewport().setBackground(new Color(18, 24, 38));
        scroll.setPreferredSize(new Dimension(800, 200));

        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new EsquinaTablaOscura(new Color(11, 15, 25)));
        estilarScroll(scroll.getVerticalScrollBar());

        card.add(topBar, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel crearCardFinalizar() {
        CardPanel card = new CardPanel(14, new Color(18, 24, 38));
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel lblTitle = new JLabel("FINALIZAR EL MANTENIMIENTO SELECCIONADO");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(56, 189, 248));

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 6, 2, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        body.add(crearLabelCampo("Resultado"), gbc);

        cboResultado = new JComboBox<>(ResultadoMantenimiento.values());
        estilarComboBox(cboResultado);
        cboResultado.setPreferredSize(new Dimension(160, 32));
        gbc.gridx = 1; gbc.weightx = 0.2;
        body.add(cboResultado, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        body.add(crearLabelCampo("Observaciones"), gbc);

        txtObservacionesFinales = crearInput();
        txtObservaciones = txtObservacionesFinales;

        gbc.gridx = 3; gbc.weightx = 0.6;
        body.add(txtObservacionesFinales, gbc);

        btnFinalizar = new BotonEstilizado("✓ FINALIZAR MANTENIMIENTO", new Color(248, 250, 252), new Color(15, 23, 42), new Color(255, 255, 255, 220));
        btnFinalizar.setPreferredSize(new Dimension(210, 34));
        gbc.gridx = 4; gbc.weightx = 0.2;
        body.add(btnFinalizar, gbc);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    private JPanel crearFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        btnSalir = new BotonEstilizado("➔ Salir", new Color(18, 24, 38), Color.WHITE, new Color(46, 59, 82, 180));
        btnSalir.setPreferredSize(new Dimension(100, 34));

        footer.add(btnSalir, BorderLayout.EAST);

        return footer;
    }

    private JLabel crearLabelCampo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(new Color(148, 163, 184));
        return l;
    }

    private JTextField crearInput() {
        JTextField tf = new JTextField();
        tf.setBackground(new Color(18, 24, 38));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        LineBorder borderNormal = new LineBorder(new Color(46, 59, 82), 1, true);
        LineBorder borderFocus = new LineBorder(new Color(59, 130, 246), 2, true);

        tf.setBorder(BorderFactory.createCompoundBorder(borderNormal, new EmptyBorder(4, 8, 4, 8)));

        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tf.isEditable()) {
                    tf.setBorder(BorderFactory.createCompoundBorder(borderFocus, new EmptyBorder(3, 7, 3, 7)));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(tf.isEditable() ? new Color(46, 59, 82) : new Color(30, 41, 59), 1, true),
                        new EmptyBorder(4, 8, 4, 8)));
            }
        });

        return tf;
    }

    private <T> void estilarComboBox(JComboBox<T> combo) {
        combo.setBackground(new Color(18, 24, 38));
        combo.setForeground(Color.WHITE);
        combo.setFont(new Font("Segoe UI", Font.BOLD, 12));

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setOpaque(true);
                l.setFont(new Font("Segoe UI", Font.BOLD, 11));
                l.setBorder(new EmptyBorder(5, 8, 5, 8));

                if (isSelected) {
                    l.setBackground(new Color(30, 58, 138));
                    l.setForeground(Color.WHITE);
                } else {
                    l.setBackground(new Color(18, 24, 38));
                    l.setForeground(new Color(226, 232, 240));
                }
                return l;
            }
        });
    }

    private void configurarTabla() {
        String[] columnas = {"N°", "EQUIPO", "TIPO", "INGRESO", "RESPONSABLE", "DESCRIPCIÓN", "ESTADO", "RESULTADO", "SALIDA", "ESTADO DEL EQUIPO"};
        modelTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblMantenimientos.setModel(modelTabla);
        tblMantenimientos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblMantenimientos.setBackground(new Color(18, 24, 38));
        tblMantenimientos.setForeground(new Color(226, 232, 240));
        tblMantenimientos.setSelectionBackground(new Color(30, 41, 59));
        tblMantenimientos.setSelectionForeground(Color.WHITE);
        tblMantenimientos.setRowHeight(42);
        tblMantenimientos.setShowGrid(false);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setOpaque(true);
                l.setBackground(new Color(11, 15, 25));
                l.setForeground(new Color(148, 163, 184));
                l.setFont(new Font("Segoe UI", Font.BOLD, 10));
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(46, 59, 82)));
                return l;
            }
        };

        if (tblMantenimientos.getTableHeader() != null) {
            tblMantenimientos.getTableHeader().setBackground(new Color(11, 15, 25));
            tblMantenimientos.getTableHeader().setOpaque(true);
            tblMantenimientos.getTableHeader().setPreferredSize(new Dimension(0, 32));
            tblMantenimientos.getTableHeader().setDefaultRenderer(headerRenderer);
        }

        tblMantenimientos.getColumnModel().getColumn(0).setCellRenderer(new BadgeIdRenderer());
        tblMantenimientos.getColumnModel().getColumn(1).setCellRenderer(new EquipoNombreRenderer());
        tblMantenimientos.getColumnModel().getColumn(2).setCellRenderer(new ChipTipoRenderer());
        tblMantenimientos.getColumnModel().getColumn(6).setCellRenderer(new EstadoMantenimientoRenderer());
        tblMantenimientos.getColumnModel().getColumn(7).setCellRenderer(new ResultadoMantenimientoRenderer());
        tblMantenimientos.getColumnModel().getColumn(9).setCellRenderer(new EstadoEquipoRenderer());

        DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setOpaque(true);

                if (isSelected) {
                    c.setBackground(new Color(30, 41, 59));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31));
                    c.setForeground(new Color(226, 232, 240));
                }
                c.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                c.setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };

        for (int i = 0; i < tblMantenimientos.getColumnCount(); i++) {
            if (i != 0 && i != 1 && i != 2 && i != 6 && i != 7 && i != 9) {
                tblMantenimientos.getColumnModel().getColumn(i).setCellRenderer(defaultRenderer);
            }
        }
    }

    private void estilarScroll(JScrollBar scrollBar) {
        if (scrollBar == null) return;
        scrollBar.setBackground(new Color(18, 24, 38));
        scrollBar.setOpaque(true);
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(51, 65, 85);
                this.trackColor = new Color(18, 24, 38);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return crearBotonVacio();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return crearBotonVacio();
            }

            private JButton crearBotonVacio() {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                return btn;
            }
        });
    }

    private void configurarEventos() {
        btnBuscarEquipo.addActionListener(e -> buscarEquipo());
        txtCodigoEquipo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) buscarEquipo();
            }
        });

        btnEnviar.addActionListener(e -> enviarAMantenimiento());
        chkSoloEnProceso.addActionListener(e -> cargarTabla());
        btnFinalizar.addActionListener(e -> finalizarMantenimiento());

        btnSalir.addActionListener(e -> {
            Window win = SwingUtilities.getWindowAncestor(panelPrincipal);
            if (win != null) win.dispose();
        });
    }

    private void buscarEquipo() {
        String codigo = txtCodigoEquipo.getText().trim();
        if (codigo.isBlank()) {
            JOptionPane.showMessageDialog(panelPrincipal, "Ingrese el código de inventario del equipo.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Equipo eq = mantenimientoController.buscarEquipo(codigo);

            if (eq != null) {
                equipoSeleccionado = eq;
                lblInfoEquipo.setText("● " + equipoSeleccionado.getCodigoInventario() + " - " + equipoSeleccionado.getNombre());
                lblInfoEquipo.setForeground(new Color(52, 211, 153));
                if (lblEquipo != null) lblEquipo.setText(lblInfoEquipo.getText());
            } else {
                equipoSeleccionado = null;
                lblInfoEquipo.setText("No encontrado");
                lblInfoEquipo.setForeground(new Color(248, 113, 113));
                if (lblEquipo != null) lblEquipo.setText("No encontrado");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void enviarAMantenimiento() {
        if (equipoSeleccionado == null) {
            JOptionPane.showMessageDialog(panelPrincipal, "Primero debe buscar y seleccionar un equipo válido.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String responsable = txtResponsable.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        if (responsable.isBlank() || descripcion.isBlank()) {
            JOptionPane.showMessageDialog(panelPrincipal, "Ingrese el responsable y la descripción del mantenimiento.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            mantenimientoController.enviarAMantenimiento(
                    equipoSeleccionado.getCodigoInventario(),
                    (TipoMantenimiento) cboTipo.getSelectedItem(),
                    descripcion,
                    responsable
            );

            JOptionPane.showMessageDialog(panelPrincipal, "Equipo enviado a mantenimiento exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormularioIngreso();
            cargarTabla();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarTabla() {
        try {
            boolean soloActivos = chkSoloEnProceso.isSelected();
            List<Mantenimiento> lista = mantenimientoController.listar(soloActivos);

            modelTabla.setRowCount(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (Mantenimiento m : lista) {
                modelTabla.addRow(new Object[]{
                        m.getIdMantenimiento(),
                        m.getEquipo(),
                        m.getTipo(),
                        m.getFechaIngreso() != null ? m.getFechaIngreso().format(fmt) : "-",
                        m.getResponsable(),
                        m.getDescripcion(),
                        m.getEstado(),
                        m.getResultado() != null ? m.getResultado() : "PENDIENTE",
                        m.getFechaSalida() != null ? m.getFechaSalida().format(fmt) : "-",
                        m.getEquipo() != null ? m.getEquipo().getEstado() : "-"
                });
            }

            lblContadorRegistros.setText("REGISTROS ACTIVOS: " + lista.size() + " | FILTRADO: " + (soloActivos ? "ACTIVO" : "TODOS"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void finalizarMantenimiento() {
        int row = tblMantenimientos.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(panelPrincipal, "Seleccione un mantenimiento de la tabla para finalizar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idMantenimiento = (int) tblMantenimientos.getValueAt(row, 0);
        ResultadoMantenimiento resultado = (ResultadoMantenimiento) cboResultado.getSelectedItem();
        String obs = txtObservacionesFinales.getText().trim();

        try {
            mantenimientoController.finalizar(idMantenimiento, resultado, obs);

            JOptionPane.showMessageDialog(panelPrincipal, "Mantenimiento finalizado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            txtObservacionesFinales.setText("");
            cargarTabla();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormularioIngreso() {
        txtCodigoEquipo.setText("");
        lblInfoEquipo.setText("Sin equipo identificado");
        lblInfoEquipo.setForeground(new Color(148, 163, 184));
        if (lblEquipo != null) lblEquipo.setText("Sin equipo identificado");
        txtResponsable.setText("");
        txtDescripcion.setText("");
        equipoSeleccionado = null;
    }

    public JPanel getPanelPrincipal() {
        return panelPrincipal;
    }

    // ========================================================
    // COMPONENTES Y RENDERERS PERSONALIZADOS
    // ========================================================

    private static class EsquinaTablaOscura extends JComponent {
        private final Color colorFundo;

        public EsquinaTablaOscura(Color colorFundo) {
            this.colorFundo = colorFundo;
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(colorFundo);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(46, 59, 82));
            g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        }
    }

    private static class IconoCirculoDorado implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = 26;
            int cx = x + size / 2;
            int cy = y + size / 2;

            g2.setColor(new Color(45, 50, 60));
            g2.fillOval(x, y, size, size);

            Color dorado = new Color(245, 158, 11);
            g2.setColor(dorado);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2.drawOval(cx - 6, cy - 6, 12, 12);
            g2.drawLine(cx, cy - 10, cx, cy + 10);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 26; }

        @Override
        public int getIconHeight() { return 26; }
    }

    private static class BotonEstilizado extends JButton {
        private final Color bgColor;
        private final Color fgColor;
        private final Color glowColor;

        public BotonEstilizado(String text, Color bg, Color fg, Color glow) {
            super(text);
            this.bgColor = bg;
            this.fgColor = fg;
            this.glowColor = glow;
            setFont(new Font("Segoe UI Symbol", Font.BOLD, 11));
            setForeground(fgColor);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorder(new EmptyBorder(6, 12, 6, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(glowColor);
            g2.fillRoundRect(1, 2, w - 2, h - 2, 8, 8);

            g2.setColor(getModel().isPressed() ? bgColor.darker() : bgColor);
            g2.fillRoundRect(0, 0, w - 1, h - 2, 8, 8);

            g2.setColor(new Color(255, 255, 255, 40));
            g2.drawRoundRect(0, 0, w - 2, h - 3, 8, 8);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class CardPanel extends JPanel {
        private final int cornerRadius;
        private final Color bgColor;

        public CardPanel(int radius, Color bg) {
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
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius);

            g2.dispose();
        }
    }

    private static class BadgeIdRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(false);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));

            Color colorBase = PALETA_COLORES[row % PALETA_COLORES.length];
            label.setForeground(colorBase);

            JPanel panel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth() - 14;
                    int h = getHeight() - 12;
                    int x = (getWidth() - w) / 2;
                    int y = (getHeight() - h) / 2;

                    g2.setColor(new Color(colorBase.getRed(), colorBase.getGreen(), colorBase.getBlue(), 35));
                    g2.fillRoundRect(x, y, w, h, 8, 8);

                    g2.setColor(colorBase);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(x, y, w - 1, h - 1, 8, 8);

                    g2.dispose();
                }
            };

            panel.setOpaque(true);
            panel.setBackground(isSelected ? new Color(30, 41, 59) : (row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31)));
            panel.add(label, BorderLayout.CENTER);
            return panel;
        }
    }

    private static class EquipoNombreRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(new Color(56, 189, 248));
            label.setHorizontalAlignment(SwingConstants.CENTER);

            if (isSelected) {
                label.setBackground(new Color(30, 41, 59));
            } else {
                label.setBackground(row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31));
            }

            return label;
        }
    }

    private static class ChipTipoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            label.setForeground(new Color(226, 232, 240));
            label.setHorizontalAlignment(SwingConstants.CENTER);

            if (isSelected) {
                label.setBackground(new Color(30, 41, 59));
            } else {
                label.setBackground(row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31));
            }

            return label;
        }
    }

    private static class EstadoMantenimientoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(false);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 10));

            String st = value != null ? value.toString().toUpperCase() : "";

            JPanel panel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    boolean enProceso = st.contains("PROCESO") || st.contains("INICIADO");
                    Color bg = enProceso ? new Color(110, 70, 15) : new Color(6, 78, 59);
                    Color border = enProceso ? new Color(245, 158, 11) : new Color(52, 211, 153);

                    int w = getWidth() - 14;
                    int h = getHeight() - 12;
                    int x = (getWidth() - w) / 2;
                    int y = (getHeight() - h) / 2;

                    g2.setColor(bg);
                    g2.fillRoundRect(x, y, w, h, 8, 8);

                    g2.setColor(border);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(x, y, w - 1, h - 1, 8, 8);

                    g2.dispose();
                }
            };

            panel.setOpaque(true);
            panel.setBackground(isSelected ? new Color(30, 41, 59) : (row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31)));

            label.setText(st);
            label.setForeground(st.contains("PROCESO") || st.contains("INICIADO") ? new Color(245, 158, 11) : new Color(52, 211, 153));

            panel.add(label, BorderLayout.CENTER);
            return panel;
        }
    }

    private static class ResultadoMantenimientoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 10));
            label.setHorizontalAlignment(SwingConstants.CENTER);

            String res = value != null ? value.toString().toUpperCase() : "PENDIENTE";
            label.setText(res);

            if (res.contains("EXITOSO") || res.contains("CORRECTO") || res.contains("REPARADO")) {
                label.setForeground(new Color(52, 211, 153));
            } else if (res.contains("PENDIENTE")) {
                label.setForeground(new Color(148, 163, 184));
            } else {
                label.setForeground(new Color(248, 113, 113));
            }

            if (isSelected) {
                label.setBackground(new Color(30, 41, 59));
            } else {
                label.setBackground(row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31));
            }

            return label;
        }
    }

    private static class EstadoEquipoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 10));
            label.setHorizontalAlignment(SwingConstants.CENTER);

            String st = value != null ? value.toString().toUpperCase() : "";
            label.setText(st);

            if (st.contains("DISPONIBLE")) {
                label.setForeground(new Color(52, 211, 153));
            } else if (st.contains("MANTENIMIENTO")) {
                label.setForeground(new Color(245, 158, 11));
            } else {
                label.setForeground(new Color(248, 113, 113));
            }

            if (isSelected) {
                label.setBackground(new Color(30, 41, 59));
            } else {
                label.setBackground(row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31));
            }

            return label;
        }
    }
}