package universidad.sigelab.view;

import universidad.sigelab.controller.LaboratorioController;
import universidad.sigelab.enums.EstadoRegistro;
import universidad.sigelab.model.Laboratorio;
import universidad.sigelab.model.Usuario;
import universidad.sigelab.util.BusinessException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LaboratorioView {

    private JPanel panelPrincipal;

    private JTextField txtId;
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextField txtUbicacion;
    private JTextField txtCapacidad;

    private JCheckBox chkActivo;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnActivarDesactivar;

    private JTextField txtBuscar;
    private JButton btnBuscar;

    private JTable tblLaboratorios;
    private JScrollPane scrollTabla;
    private JButton btnSalir;

    // Métricas KPI
    private JLabel lblTotalLabsCount;
    private JLabel lblActivosCount;
    private JLabel lblInactivosCount;
    private JLabel lblCapacidadCount;
    private JLabel lblDisponibilidadCount;
    private JLabel lblFooterStats;

    private Usuario usuarioActual;
    private final LaboratorioController laboratorioController;
    private List<Laboratorio> listaLaboratoriosActuales = new ArrayList<>();

    // Paleta de colores Dark SaaS para rotación en cada laboratorio
    private static final Color[] PALETA_COLORES = {
            new Color(56, 189, 248),  // Cian / Azul claro
            new Color(16, 185, 129),  // Esmeralda / Verde
            new Color(245, 158, 11),  // Ámbar / Naranja
            new Color(139, 92, 246),  // Púrpura
            new Color(236, 72, 153),  // Rosa
            new Color(20, 184, 166),  // Teal
            new Color(99, 102, 241)   // Índigo
    };

    public LaboratorioView() {
        this(null, new LaboratorioController());
    }

    public LaboratorioView(Usuario usuario) {
        this(usuario, new LaboratorioController());
    }

    public LaboratorioView(LaboratorioController laboratorioController) {
        this(null, laboratorioController);
    }

    public LaboratorioView(Usuario usuario, LaboratorioController laboratorioController) {
        this.usuarioActual = usuario;
        this.laboratorioController = laboratorioController;

        initComponents();
        configurarFormulario();
        configurarEventos();
        cargarLaboratorios();
    }

    private void initComponents() {
        panelPrincipal = new JPanel(new BorderLayout(0, 10));
        panelPrincipal.setBackground(new Color(11, 15, 25));
        panelPrincipal.setBorder(new EmptyBorder(12, 16, 12, 16));

        panelPrincipal.add(crearHeader(), BorderLayout.NORTH);

        JPanel centroPanel = new JPanel(new BorderLayout(0, 10));
        centroPanel.setOpaque(false);

        JPanel superioresPanel = new JPanel();
        superioresPanel.setLayout(new BoxLayout(superioresPanel, BoxLayout.Y_AXIS));
        superioresPanel.setOpaque(false);

        superioresPanel.add(crearPanelKpi());
        superioresPanel.add(Box.createVerticalStrut(10));
        superioresPanel.add(crearCardFormulario());

        centroPanel.add(superioresPanel, BorderLayout.NORTH);
        centroPanel.add(crearCardTabla(), BorderLayout.CENTER);

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
                // Borde izquierdo verde azulado / esmeralda
                g2.setColor(new Color(45, 212, 191));
                g2.fillRect(0, 0, 4, getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(4, 12, 4, 12));

        // Contenedor del título e ícono a la par en el lado izquierdo
        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftBox.setOpaque(false);

        JPanel textGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        textGroup.setOpaque(false);

        JLabel lblTitle = new JLabel("LABORATORIOS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Infraestructura física — Gestión de capacidad operativa y asignación de espacios");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184));

        textGroup.add(lblTitle);
        textGroup.add(lblSub);

        JLabel lblIcono = new JLabel(new IconoInfraestructuraLab(new Color(45, 212, 191)));

        leftBox.add(textGroup);
        leftBox.add(lblIcono);

        header.add(leftBox, BorderLayout.WEST);
        header.add(crearBadgeUsuarioDinamico(usuarioActual), BorderLayout.EAST);

        return header;
    }

    private JPanel crearBadgeUsuarioDinamico(Usuario usuario) {
        JPanel infoBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
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
        lblOperador.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
        lblOperador.setForeground(Color.WHITE);

        JLabel lblRolTag = new JLabel(rolUser.toUpperCase());
        lblRolTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblRolTag.setForeground(Color.WHITE);
        lblRolTag.setOpaque(true);
        lblRolTag.setBackground(obtenerColorRol(rolUser, nombreUser));
        lblRolTag.setBorder(new EmptyBorder(3, 8, 3, 8));

        infoBox.add(lblOperador);
        infoBox.add(lblRolTag);

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

    private JPanel crearPanelKpi() {
        JPanel kpiGrid = new JPanel(new GridLayout(1, 5, 8, 0));
        kpiGrid.setOpaque(false);

        lblTotalLabsCount = new JLabel("0");
        lblActivosCount = new JLabel("0");
        lblInactivosCount = new JLabel("0");
        lblCapacidadCount = new JLabel("0");
        lblDisponibilidadCount = new JLabel("0%");

        kpiGrid.add(crearCardKpi("TOTAL LABS", lblTotalLabsCount, "Registrados", "🧪", new Color(14, 116, 144)));
        kpiGrid.add(crearCardKpi("ACTIVADOS", lblActivosCount, "Habilitados", "✅", new Color(16, 185, 129)));
        kpiGrid.add(crearCardKpi("DESACTIVADOS", lblInactivosCount, "Inhabilitados", "🚫", new Color(239, 68, 68)));
        kpiGrid.add(crearCardKpi("CAPACIDAD", lblCapacidadCount, "Puestos totales", "🪑", new Color(30, 58, 138)));
        kpiGrid.add(crearCardKpi("DISPONIBILIDAD", lblDisponibilidadCount, "% de operativos", "⚡", new Color(6, 95, 70)));

        return kpiGrid;
    }

    private JPanel crearCardKpi(String titulo, JLabel lblValor, String subtexto, String icono, Color accentColor) {
        CardPanel card = new CardPanel(12, new Color(18, 24, 38));
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(8, 10, 8, 10));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel(titulo);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblTitle.setForeground(new Color(148, 163, 184));

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblValor.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel(subtexto);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(new Color(100, 116, 139));

        left.add(lblTitle);
        left.add(Box.createVerticalStrut(1));
        left.add(lblValor);
        left.add(Box.createVerticalStrut(1));
        left.add(lblSub);

        JLabel lblIcon = new JLabel(icono);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        lblIcon.setOpaque(true);
        lblIcon.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 50));
        lblIcon.setForeground(accentColor.brighter());
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setPreferredSize(new Dimension(32, 32));
        lblIcon.setBorder(BorderFactory.createLineBorder(accentColor, 1, true));

        JPanel iconWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        iconWrapper.setOpaque(false);
        iconWrapper.add(lblIcon);

        card.add(left, BorderLayout.CENTER);
        card.add(iconWrapper, BorderLayout.EAST);

        return card;
    }

    private JPanel crearCardFormulario() {
        CardPanel card = new CardPanel(14, new Color(18, 24, 38));
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel lblHeader = new JLabel(" DATOS DEL LABORATORIO // REGISTRO Y EDICIÓN ");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblHeader.setForeground(new Color(56, 189, 248));
        lblHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(14, 116, 144), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));

        chkActivo = new JCheckBox("✓ ACTIVO");
        chkActivo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chkActivo.setForeground(new Color(56, 189, 248));
        chkActivo.setOpaque(true);
        chkActivo.setBackground(new Color(15, 23, 42));
        chkActivo.setFocusPainted(false);
        chkActivo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(56, 189, 248, 180), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);
        topHeader.add(lblHeader, BorderLayout.WEST);
        topHeader.add(chkActivo, BorderLayout.EAST);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtId = crearInput();
        txtCodigo = crearInput();
        txtNombre = crearInput();
        txtUbicacion = crearInput();
        txtCapacidad = crearInput();

        /*
         * El ID lo asigna la base de datos al guardar (IDENTITY), así que el usuario no lo escribe.
         * El campo sigue en el formulario, pero oculto: al seleccionar una fila guarda el ID del
         * laboratorio que se va a actualizar o desactivar. En la tabla el ID sí se muestra.
         * GridBagLayout no les reserva espacio a los componentes ocultos.
         */
        JLabel lblId = crearLabelCampo("ID");
        lblId.setVisible(false);
        txtId.setVisible(false);

        gbc.gridx = 0; gbc.gridy = 2;
        grid.add(lblId, gbc);

        gbc.gridx = 1;
        grid.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.08;
        grid.add(crearLabelCampo("CÓDIGO *"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.15;
        grid.add(txtCodigo, gbc);

        gbc.gridx = 2; gbc.weightx = 0.10;
        grid.add(crearLabelCampo("NOMBRE *"), gbc);

        gbc.gridx = 3; gbc.gridwidth = 3; gbc.weightx = 0.67;
        grid.add(txtNombre, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.08;
        grid.add(crearLabelCampo("UBICACIÓN *"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 0.47;
        grid.add(txtUbicacion, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 4; gbc.weightx = 0.10;
        grid.add(crearLabelCampo("CAPACIDAD"), gbc);

        gbc.gridx = 5; gbc.weightx = 0.35;
        grid.add(txtCapacidad, gbc);

        JPanel panelBotones = new JPanel(new GridLayout(1, 4, 10, 0));
        panelBotones.setOpaque(false);

        btnNuevo = new BotonEstilizado("+ NUEVO", new Color(18, 24, 38), new Color(56, 189, 248), new Color(30, 58, 138, 180));
        btnGuardar = new BotonEstilizado("🔒 GUARDAR", Color.WHITE, new Color(15, 23, 42), new Color(255, 255, 255, 220));
        btnActualizar = new BotonEstilizado("↺ ACTUALIZAR", new Color(18, 24, 38), new Color(96, 165, 250), new Color(30, 58, 138, 180));
        btnActivarDesactivar = new BotonEstilizado("🚫 DESACTIVAR", new Color(18, 24, 38), new Color(248, 113, 113), new Color(127, 29, 29, 180));

        panelBotones.add(btnNuevo);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnActivarDesactivar);

        card.add(topHeader, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        card.add(panelBotones, BorderLayout.SOUTH);

        return card;
    }

    private JPanel crearCardTabla() {
        CardPanel card = new CardPanel(14, new Color(18, 24, 38));
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setOpaque(false);

        JLabel lblBuscarLabel = new JLabel("BUSCAR POR CÓDIGO:");
        lblBuscarLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblBuscarLabel.setForeground(new Color(226, 232, 240));

        txtBuscar = crearInput();

        btnBuscar = new BotonEstilizado("🔍 BUSCAR", new Color(56, 189, 248), new Color(11, 15, 25), new Color(56, 189, 248, 180));
        btnBuscar.setPreferredSize(new Dimension(110, 32));

        searchBar.add(lblBuscarLabel, BorderLayout.WEST);
        searchBar.add(txtBuscar, BorderLayout.CENTER);
        searchBar.add(btnBuscar, BorderLayout.EAST);

        tblLaboratorios = new JTable();
        configurarTabla();

        scrollTabla = new JScrollPane(tblLaboratorios);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());
        scrollTabla.getViewport().setBackground(new Color(18, 24, 38));

        estilarScroll(scrollTabla.getVerticalScrollBar());

        card.add(searchBar, BorderLayout.NORTH);
        card.add(scrollTabla, BorderLayout.CENTER);

        return card;
    }

    private JPanel crearFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        lblFooterStats = new JLabel("● Total: 0 laboratorios registrados | Capacidad acumulada: 0 espacios");
        lblFooterStats.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblFooterStats.setForeground(new Color(56, 189, 248));

        btnSalir = new BotonEstilizado("➔ Salir", new Color(18, 24, 38), Color.WHITE, new Color(46, 59, 82, 180));
        btnSalir.setPreferredSize(new Dimension(100, 32));

        footer.add(lblFooterStats, BorderLayout.WEST);
        footer.add(btnSalir, BorderLayout.EAST);

        return footer;
    }

    private JLabel crearLabelCampo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(new Color(226, 232, 240));
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

    private void configurarTabla() {
        tblLaboratorios.setModel(new TablaSoloLectura("ID", "CÓDIGO", "NOMBRE", "UBICACIÓN", "CAPACIDAD", "ACTIVO"));
        tblLaboratorios.setBackground(new Color(18, 24, 38));
        tblLaboratorios.setForeground(new Color(226, 232, 240));
        tblLaboratorios.setSelectionBackground(new Color(30, 41, 59));
        tblLaboratorios.setSelectionForeground(Color.WHITE);
        tblLaboratorios.setRowHeight(38);
        tblLaboratorios.setShowGrid(false);
        tblLaboratorios.setFillsViewportHeight(true);

        if (tblLaboratorios.getTableHeader() != null) {
            tblLaboratorios.getTableHeader().setBackground(new Color(11, 15, 25));
            tblLaboratorios.getTableHeader().setForeground(new Color(148, 163, 184));
            tblLaboratorios.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 10));
            tblLaboratorios.getTableHeader().setPreferredSize(new Dimension(0, 32));
        }

        tblLaboratorios.getColumnModel().getColumn(0).setCellRenderer(new BadgeIdLabRenderer());
        tblLaboratorios.getColumnModel().getColumn(1).setCellRenderer(new ChipCodigoLabRenderer());
        tblLaboratorios.getColumnModel().getColumn(4).setCellRenderer(new CapacidadTagRenderer());
        tblLaboratorios.getColumnModel().getColumn(5).setCellRenderer(new PillEstadoActivoRenderer());
    }

    private void estilarScroll(JScrollBar scrollBar) {
        if (scrollBar == null) return;
        scrollBar.setBackground(new Color(18, 24, 38));
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

    private void configurarFormulario() {
        txtId.setEditable(false);
        chkActivo.setEnabled(false);
        tblLaboratorios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        limpiarFormulario();
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnActivarDesactivar.addActionListener(e -> cambiarEstado());
        btnBuscar.addActionListener(e -> buscar());
        btnSalir.addActionListener(e -> salir());

        tblLaboratorios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarLaboratorio();
        });
    }

    private void guardar() {
        try {
            Laboratorio laboratorio = new Laboratorio(txtCodigo.getText(), txtNombre.getText(),
                    txtUbicacion.getText(), leerCapacidad());

            if (laboratorioController.guardar(laboratorio)) {
                JOptionPane.showMessageDialog(panelPrincipal, "Laboratorio guardado correctamente.", "Laboratorio", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                cargarLaboratorios();
            } else {
                JOptionPane.showMessageDialog(panelPrincipal, "No se pudo guardar el laboratorio.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarLaboratorios() {
        try {
            mostrarEnTabla(laboratorioController.listar());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarEnTabla(List<Laboratorio> laboratorios) {
        this.listaLaboratoriosActuales = new ArrayList<>(laboratorios);
        DefaultTableModel modelo = (DefaultTableModel) tblLaboratorios.getModel();
        modelo.setRowCount(0);

        int totalLabs = laboratorios.size();
        int capacidadTotal = 0;
        int activos = 0;
        int inactivos = 0;

        for (int i = 0; i < laboratorios.size(); i++) {
            Laboratorio laboratorio = laboratorios.get(i);
            boolean esActivo = laboratorio.getEstado() == EstadoRegistro.ACTIVO;

            modelo.addRow(new Object[]{
                    i + 1,
                    laboratorio.getCodigo(),
                    laboratorio.getNombre(),
                    laboratorio.getUbicacion(),
                    laboratorio.getCapacidad() + " puestos",
                    esActivo ? "Sí" : "No"
            });

            capacidadTotal += laboratorio.getCapacidad();
            if (esActivo) {
                activos++;
            } else {
                inactivos++;
            }
        }

        int disponibilidadPct = totalLabs > 0 ? (int) Math.round(((double) activos / totalLabs) * 100) : 0;

        lblTotalLabsCount.setText(String.valueOf(totalLabs));
        lblActivosCount.setText(String.valueOf(activos));
        lblInactivosCount.setText(String.valueOf(inactivos));
        lblCapacidadCount.setText(String.valueOf(capacidadTotal));
        lblDisponibilidadCount.setText(disponibilidadPct + "%");

        lblFooterStats.setText("● Total: " + totalLabs + " laboratorios registrados | Capacidad acumulada: " + capacidadTotal + " espacios");
    }

    private void actualizar() {
        if (txtId.getText().isBlank()) {
            JOptionPane.showMessageDialog(panelPrincipal, "Debe seleccionar un laboratorio.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Laboratorio laboratorio = new Laboratorio(Integer.parseInt(txtId.getText()), txtCodigo.getText(),
                    txtNombre.getText(), txtUbicacion.getText(), leerCapacidad(), null);

            if (laboratorioController.actualizar(laboratorio)) {
                JOptionPane.showMessageDialog(panelPrincipal, "Laboratorio actualizado correctamente.", "Laboratorio", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                cargarLaboratorios();
            } else {
                JOptionPane.showMessageDialog(panelPrincipal, "No se pudo actualizar el laboratorio.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cambiarEstado() {
        if (txtId.getText().isBlank()) {
            JOptionPane.showMessageDialog(panelPrincipal, "Debe seleccionar un laboratorio.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean activar = !chkActivo.isSelected();
        String accion = activar ? "activar" : "desactivar";
        int resp = JOptionPane.showConfirmDialog(panelPrincipal, "¿Está seguro de " + accion + " el laboratorio?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (resp != JOptionPane.YES_OPTION) return;

        try {
            int idLaboratorio = Integer.parseInt(txtId.getText());
            EstadoRegistro nuevoEstado = activar ? EstadoRegistro.ACTIVO : EstadoRegistro.INACTIVO;

            if (laboratorioController.cambiarEstado(idLaboratorio, nuevoEstado)) {
                JOptionPane.showMessageDialog(panelPrincipal,
                        activar ? "Laboratorio activado correctamente." : "Laboratorio desactivado correctamente.",
                        "Laboratorio", JOptionPane.INFORMATION_MESSAGE);
                cargarLaboratorios();
                seleccionarFilaPorId(idLaboratorio);
            } else {
                JOptionPane.showMessageDialog(panelPrincipal, "No se pudo cambiar el estado del laboratorio.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscar() {
        String codigo = txtBuscar.getText().trim();

        if (codigo.isBlank()) {
            limpiarFormulario();
            cargarLaboratorios();
            return;
        }

        try {
            Optional<Laboratorio> resultado = laboratorioController.buscarPorCodigo(codigo);

            if (resultado.isPresent()) {
                mostrarEnTabla(List.of(resultado.get()));
                seleccionarFilaPorId(resultado.get().getIdLaboratorio());
            } else {
                JOptionPane.showMessageDialog(panelPrincipal, "No se encontró ningún laboratorio con ese código.", "Búsqueda", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarLaboratorio() {
        int fila = tblLaboratorios.getSelectedRow();
        if (fila == -1 || fila >= listaLaboratoriosActuales.size()) return;

        try {
            Laboratorio seleccionado = listaLaboratoriosActuales.get(fila);
            mostrarEnFormulario(seleccionado);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarEnFormulario(Laboratorio laboratorio) {
        boolean activo = laboratorio.getEstado() == EstadoRegistro.ACTIVO;

        txtId.setText(String.valueOf(laboratorio.getIdLaboratorio()));
        txtCodigo.setText(laboratorio.getCodigo());
        txtNombre.setText(laboratorio.getNombre());
        txtUbicacion.setText(laboratorio.getUbicacion());
        txtCapacidad.setText(String.valueOf(laboratorio.getCapacidad()));
        chkActivo.setSelected(activo);
        chkActivo.setText(activo ? "✓ ACTIVO" : "🚫 INACTIVO");
        chkActivo.setForeground(activo ? new Color(56, 189, 248) : new Color(248, 113, 113));

        btnGuardar.setEnabled(false);
        btnActualizar.setEnabled(true);
        btnActivarDesactivar.setEnabled(true);
        btnActivarDesactivar.setText(activo ? "🚫 DESACTIVAR" : "✓ ACTIVAR");
    }

    private void seleccionarFilaPorId(int idLaboratorio) {
        for (int fila = 0; fila < listaLaboratoriosActuales.size(); fila++) {
            if (listaLaboratoriosActuales.get(fila).getIdLaboratorio() == idLaboratorio) {
                tblLaboratorios.setRowSelectionInterval(fila, fila);
                tblLaboratorios.scrollRectToVisible(tblLaboratorios.getCellRect(fila, 0, true));
                return;
            }
        }
    }

    private void nuevo() {
        limpiarFormulario();
        txtCodigo.requestFocus();
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtCodigo.setText("");
        txtNombre.setText("");
        txtUbicacion.setText("");
        txtCapacidad.setText("");
        txtBuscar.setText("");

        chkActivo.setSelected(true);
        chkActivo.setText("✓ ACTIVO");
        chkActivo.setForeground(new Color(56, 189, 248));
        tblLaboratorios.clearSelection();

        btnGuardar.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnActivarDesactivar.setEnabled(false);
        btnActivarDesactivar.setText("🚫 DESACTIVAR");
    }

    private int leerCapacidad() {
        try {
            return Integer.parseInt(txtCapacidad.getText().trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("La capacidad debe ser un número entero.");
        }
    }

    private void salir() {
        Window win = SwingUtilities.getWindowAncestor(panelPrincipal);
        if (win != null) win.dispose();
    }

    public JPanel getPanelPrincipal() {
        return panelPrincipal;
    }

    // ========================================================
    // ÍCONO VECTORIAL DE INFRAESTRUCTURA / LABORATORIO
    // ========================================================

    private static class IconoInfraestructuraLab implements Icon {
        private final Color color;

        public IconoInfraestructuraLab(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 35));
            g2.fillOval(x, y, 32, 32);

            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2.drawLine(x + 16, y + 8, x + 16, y + 22);   // Línea vertical central
            g2.drawLine(x + 11, y + 15, x + 21, y + 15); // Línea horizontal intermedia
            g2.drawLine(x + 9, y + 22, x + 23, y + 22);   // Base inferior

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 32; }

        @Override
        public int getIconHeight() { return 32; }
    }

    // ========================================================
    // CLASES AUXILIARES Y RENDERERS DE COLOR DINÁMICO
    // ========================================================

    private static class TablaSoloLectura extends DefaultTableModel {
        public TablaSoloLectura(Object... columnas) {
            super(columnas, 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
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

    private static class BadgeIdLabRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(false);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));

            Color colorBase = PALETA_COLORES[row % PALETA_COLORES.length];
            label.setForeground(colorBase);

            JPanel panel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int size = 24;
                    int x = (getWidth() - size) / 2;
                    int y = (getHeight() - size) / 2;

                    g2.setColor(new Color(colorBase.getRed(), colorBase.getGreen(), colorBase.getBlue(), 40));
                    g2.fillOval(x, y, size, size);

                    g2.setColor(colorBase);
                    g2.drawOval(x, y, size - 1, size - 1);

                    g2.dispose();
                }
            };

            panel.setOpaque(true);
            panel.setBackground(isSelected ? new Color(30, 41, 59) : (row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31)));
            panel.add(label, BorderLayout.CENTER);
            return panel;
        }
    }

    private static class ChipCodigoLabRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            Color colorBase = PALETA_COLORES[(row + 1) % PALETA_COLORES.length];
            label.setForeground(colorBase);
            label.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(true);
            panel.setBackground(isSelected ? new Color(30, 41, 59) : (row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31)));
            panel.add(label, BorderLayout.CENTER);
            return panel;
        }
    }

    private static class CapacidadTagRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(new Color(56, 189, 248));
            label.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(true);
            panel.setBackground(isSelected ? new Color(30, 41, 59) : (row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31)));
            panel.add(label, BorderLayout.CENTER);
            return panel;
        }
    }

    private static class PillEstadoActivoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(false);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 10));

            String st = value != null ? value.toString().toUpperCase() : "";
            boolean esActivo = st.contains("SÍ") || st.contains("SI");

            Color colorTexto = esActivo ? new Color(52, 211, 153) : new Color(248, 113, 113);
            Color colorFondo = esActivo ? new Color(6, 78, 59, 120) : new Color(127, 29, 29, 120);

            label.setForeground(colorTexto);

            JPanel panel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int h = 22;
                    int w = getWidth() - 12;
                    int x = (getWidth() - w) / 2;
                    int y = (getHeight() - h) / 2;

                    g2.setColor(colorFondo);
                    g2.fillRoundRect(x, y, w, h, 12, 12);

                    g2.setColor(colorTexto);
                    g2.drawRoundRect(x, y, w - 1, h - 1, 12, 12);

                    g2.dispose();
                }
            };

            panel.setOpaque(true);
            panel.setBackground(isSelected ? new Color(30, 41, 59) : (row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31)));
            panel.add(label, BorderLayout.CENTER);
            return panel;
        }
    }
}