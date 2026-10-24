package universidad.sigelab.view;

import universidad.sigelab.controller.ReporteController;
import universidad.sigelab.enums.EstadoEquipo;
import universidad.sigelab.model.Equipo;
import universidad.sigelab.model.HistorialEquipo;
import universidad.sigelab.model.Mantenimiento;
import universidad.sigelab.model.Prestamo;
import universidad.sigelab.model.PrestamoResumen;
import universidad.sigelab.model.Usuario;
import universidad.sigelab.util.Fechas;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDateTime;
import java.util.List;

public class ReporteView {

    private static final String TODOS = "TODOS";

    private enum TipoReporte {
        EQUIPOS_POR_ESTADO("Equipos por estado"),
        PRESTAMOS_ACTIVOS("Préstamos activos"),
        PRESTAMOS_FINALIZADOS("Préstamos finalizados"),
        PRESTAMOS_VENCIDOS("Préstamos vencidos"),
        PRESTAMOS_ENTRE_FECHAS("Préstamos entre fechas"),
        HISTORIAL_DE_EQUIPO("Historial de préstamos de un equipo"),
        HISTORIAL_DE_MANTENIMIENTO("Historial de mantenimiento");

        private final String texto;

        TipoReporte(String texto) {
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private JPanel panelPrincipal;
    private JPanel headerContainer;

    private JComboBox<TipoReporte> cboReporte;
    private JComboBox<Object> cboEstado;
    private JTextField txtCodigoEquipo;
    private JSpinner spnDesde;
    private JSpinner spnHasta;
    private JButton btnConsultar;
    private JLabel lblTotal;

    private JTable tblReporte;
    private JButton btnSalir;

    private Usuario usuarioActual;
    private final ReporteController reporteController;

    private static final Color COLOR_BG_CAMPO = new Color(18, 24, 38);
    private static final Color COLOR_BG_DESACTIVADO = new Color(13, 17, 27);
    private static final Color COLOR_BORDER = new Color(46, 59, 82);
    private static final Color COLOR_SELECCION_FILA = new Color(30, 58, 138);

    private static final Color[] PALETA_COLORES = {
            new Color(59, 130, 246),
            new Color(16, 185, 129),
            new Color(6, 182, 212),
            new Color(245, 158, 11),
            new Color(139, 92, 246),
            new Color(236, 72, 153),
            new Color(20, 184, 166),
            new Color(99, 102, 241)
    };

    public ReporteView() {
        this(null, new ReporteController());
    }

    public ReporteView(Usuario usuario) {
        this(usuario, new ReporteController());
    }

    public ReporteView(ReporteController reporteController) {
        this(null, reporteController);
    }

    public ReporteView(Usuario usuario, ReporteController reporteController) {
        this.usuarioActual = usuario;
        this.reporteController = reporteController;

        aplicarEstiloGlobalSwing();

        initComponents();
        configurarFormulario();
        configurarEventos();
        consultar();
    }

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
        if (headerContainer != null) {
            headerContainer.removeAll();
            headerContainer.add(crearHeaderContenido());
            headerContainer.revalidate();
            headerContainer.repaint();
        }
    }

    private void aplicarEstiloGlobalSwing() {
        try {
            UIManager.put("ComboBox.background", COLOR_BG_CAMPO);
            UIManager.put("ComboBox.disabledBackground", COLOR_BG_DESACTIVADO);
            UIManager.put("ComboBox.foreground", Color.WHITE);
            UIManager.put("ComboBox.disabledForeground", new Color(100, 116, 139));
            UIManager.put("ComboBox.selectionBackground", COLOR_SELECCION_FILA);
            UIManager.put("ComboBox.selectionForeground", Color.WHITE);

            UIManager.put("Spinner.background", COLOR_BG_CAMPO);
            UIManager.put("Spinner.disabledBackground", COLOR_BG_DESACTIVADO);
            UIManager.put("Spinner.foreground", Color.WHITE);

            UIManager.put("FormattedTextField.background", COLOR_BG_CAMPO);
            UIManager.put("FormattedTextField.disabledBackground", COLOR_BG_DESACTIVADO);
            UIManager.put("FormattedTextField.inactiveBackground", COLOR_BG_DESACTIVADO);
            UIManager.put("FormattedTextField.foreground", Color.WHITE);
            UIManager.put("FormattedTextField.disabledForeground", new Color(100, 116, 139));

            UIManager.put("TextField.background", COLOR_BG_CAMPO);
            UIManager.put("TextField.disabledBackground", COLOR_BG_DESACTIVADO);
            UIManager.put("TextField.inactiveBackground", COLOR_BG_DESACTIVADO);
            UIManager.put("TextField.foreground", Color.WHITE);
            UIManager.put("TextField.disabledForeground", new Color(100, 116, 139));

            UIManager.put("Table.selectionBackground", COLOR_SELECCION_FILA);
            UIManager.put("Table.selectionForeground", Color.WHITE);
        } catch (Exception ignored) {}
    }

    private void initComponents() {
        panelPrincipal = new JPanel(new BorderLayout(0, 10));
        panelPrincipal.setBackground(new Color(11, 15, 25));
        panelPrincipal.setBorder(new EmptyBorder(12, 16, 12, 16));

        headerContainer = new JPanel(new BorderLayout());
        headerContainer.setOpaque(false);
        headerContainer.add(crearHeaderContenido());

        panelPrincipal.add(headerContainer, BorderLayout.NORTH);

        JPanel centroPanel = new JPanel(new BorderLayout(0, 10));
        centroPanel.setOpaque(false);

        centroPanel.add(crearCardFiltros(), BorderLayout.NORTH);
        centroPanel.add(crearCardResultados(), BorderLayout.CENTER);

        panelPrincipal.add(centroPanel, BorderLayout.CENTER);
        panelPrincipal.add(crearFooter(), BorderLayout.SOUTH);
    }

    private JPanel crearHeaderContenido() {
        JPanel header = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(168, 85, 247));
                g2.fillRect(0, 0, 4, getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(4, 12, 4, 12));

        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftBox.setOpaque(false);

        JPanel textGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        textGroup.setOpaque(false);

        JLabel lblTitle = new JLabel("REPORTES");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Gestión de reportes analíticos e historial del laboratorio");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184));

        textGroup.add(lblTitle);
        textGroup.add(lblSub);

        JLabel lblIcono = new JLabel(new IconoReportes(new Color(168, 85, 247)));

        leftBox.add(textGroup);
        leftBox.add(lblIcono);

        header.add(leftBox, BorderLayout.WEST);
        header.add(crearBadgeUsuarioDinamico(usuarioActual), BorderLayout.EAST);

        return header;
    }

    private JPanel crearBadgeUsuarioDinamico(Usuario usuario) {
        JPanel infoBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        infoBox.setOpaque(false);

        String nombreUser = "admin";
        String rolUser = "ADMIN";

        if (usuario != null) {
            if (usuario.getUsuario() != null && !usuario.getUsuario().isBlank()) {
                nombreUser = usuario.getUsuario();
            } else if (usuario.getNombreCompleto() != null && !usuario.getNombreCompleto().isBlank()) {
                nombreUser = usuario.getNombreCompleto();
            }

            if (usuario.getRol() != null) {
                if (usuario.getRol().getNombre() != null) {
                    rolUser = usuario.getRol().getNombre().name();
                } else {
                    rolUser = usuario.getRol().toString();
                }
            }
        }

        String iconoUser = obtenerIcono(rolUser, nombreUser);

        JLabel lblOperador = new JLabel(iconoUser + " " + nombreUser);
        lblOperador.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
        lblOperador.setForeground(Color.WHITE);

        JLabel lblRolTag = new JLabel(rolUser.toUpperCase());
        lblRolTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblRolTag.setForeground(Color.WHITE);
        lblRolTag.setOpaque(true);
        lblRolTag.setBackground(obtenerColorRol(rolUser));
        lblRolTag.setBorder(new EmptyBorder(3, 8, 3, 8));

        infoBox.add(lblOperador);
        infoBox.add(lblRolTag);

        return infoBox;
    }

    private String obtenerIcono(String rol, String nombreUsuario) {
        String r = rol != null ? rol.toUpperCase() : "";
        String u = nombreUsuario != null ? nombreUsuario.toLowerCase() : "";

        if (r.contains("ADMIN") || u.contains("admin")) return "👑";
        if (r.contains("DOCENTE") || r.contains("CATEDRATICO") || u.contains("docente")) return "🎓";
        if (r.contains("INACTIVO") || u.contains("inactivo")) return "🚫";
        if (r.contains("ENCARGADO") || r.contains("AUXILIAR")) return "💼";

        return "👤";
    }

    private Color obtenerColorRol(String rol) {
        if (rol == null) return new Color(75, 85, 99);

        switch (rol.toUpperCase()) {
            case "ADMIN":
            case "ADMINISTRADOR":
                return new Color(30, 58, 138);
            case "DOCENTE":
            case "CATEDRATICO":
                return new Color(13, 148, 136);
            case "INACTIVO":
                return new Color(239, 68, 68);
            default:
                return new Color(75, 85, 99);
        }
    }

    private JPanel crearCardFiltros() {
        CardPanel card = new CardPanel(14, COLOR_BG_CAMPO);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel lblHeader = new JLabel(" DATOS DE BÚSQUEDA ");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblHeader.setForeground(new Color(56, 189, 248));
        lblHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(14, 116, 144), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));

        JPanel headerBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerBox.setOpaque(false);
        headerBox.add(lblHeader);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cboReporte = crearCombo();
        cboEstado = crearCombo();

        txtCodigoEquipo = crearInput();

        spnDesde = crearSpinnerFecha();
        spnHasta = crearSpinnerFecha();

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.15;
        grid.add(crearLabelCampo("Reporte"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 0.85;
        grid.add(cboReporte, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.15;
        grid.add(crearLabelCampo("Estado del equipo"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.35;
        grid.add(cboEstado, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        grid.add(crearLabelCampo("Código de equipo"), gbc);

        gbc.gridx = 3; gbc.weightx = 0.35;
        grid.add(txtCodigoEquipo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.15;
        grid.add(crearLabelCampo("Desde"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.35;
        grid.add(spnDesde, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        grid.add(crearLabelCampo("Hasta"), gbc);

        gbc.gridx = 3; gbc.weightx = 0.35;
        grid.add(spnHasta, gbc);

        card.add(headerBox, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);

        return card;
    }

    private JPanel crearCardResultados() {
        CardPanel card = new CardPanel(14, COLOR_BG_CAMPO);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        btnConsultar = new BotonEstilizado("🔍 CONSULTAR REPORTE", new Color(30, 58, 138), Color.WHITE, new Color(59, 130, 246, 200));
        btnConsultar.setPreferredSize(new Dimension(250, 34));

        JPanel btnCenterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnCenterPanel.setOpaque(false);
        btnCenterPanel.add(btnConsultar);

        lblTotal = new JLabel("0 registro(s)");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTotal.setForeground(new Color(56, 189, 248));

        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightBox.setOpaque(false);
        rightBox.add(lblTotal);

        topBar.add(btnCenterPanel, BorderLayout.CENTER);
        topBar.add(rightBox, BorderLayout.EAST);

        tblReporte = new JTable();
        configurarEstiloTabla(tblReporte);

        JScrollPane scrollTabla = new JScrollPane(tblReporte);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());
        scrollTabla.getViewport().setBackground(COLOR_BG_CAMPO);
        estilarScroll(scrollTabla.getVerticalScrollBar());

        card.add(topBar, BorderLayout.NORTH);
        card.add(scrollTabla, BorderLayout.CENTER);

        return card;
    }

    private JPanel crearFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        btnSalir = new BotonEstilizado("➔ Salir", COLOR_BG_CAMPO, Color.WHITE, new Color(46, 59, 82, 180));
        btnSalir.setPreferredSize(new Dimension(100, 32));

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
        tf.setBackground(COLOR_BG_CAMPO);
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        LineBorder borderNormal = new LineBorder(COLOR_BORDER, 1, true);
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
                tf.setBorder(BorderFactory.createCompoundBorder(borderNormal, new EmptyBorder(4, 8, 4, 8)));
            }
        });

        return tf;
    }

    private <T> JComboBox<T> crearCombo() {
        JComboBox<T> cb = new JComboBox<T>();
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cb.setBackground(COLOR_BG_CAMPO);
        cb.setForeground(Color.WHITE);

        cb.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("▾");
                btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btn.setForeground(new Color(148, 163, 184));
                btn.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 8));
                btn.setContentAreaFilled(false);
                btn.setFocusable(false);
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(cb.isEnabled() ? COLOR_BG_CAMPO : COLOR_BG_DESACTIVADO);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });

        cb.setBorder(new LineBorder(COLOR_BORDER, 1, true));

        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setOpaque(true);
                l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                l.setBorder(new EmptyBorder(6, 10, 6, 10));

                if (index < 0) {
                    l.setBackground(cb.isEnabled() ? COLOR_BG_CAMPO : COLOR_BG_DESACTIVADO);
                    l.setForeground(cb.isEnabled() ? Color.WHITE : new Color(100, 116, 139));
                } else if (isSelected) {
                    l.setBackground(COLOR_SELECCION_FILA);
                    l.setForeground(Color.WHITE);
                } else {
                    l.setBackground(COLOR_BG_CAMPO);
                    l.setForeground(Color.WHITE);
                }
                return l;
            }
        });

        return cb;
    }

    private JSpinner crearSpinnerFecha() {
        JSpinner spn = new JSpinner(new SpinnerDateModel());
        spn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        spn.setBackground(COLOR_BG_CAMPO);

        spn.setUI(new BasicSpinnerUI() {
            @Override
            protected Component createNextButton() { return crearBotonVacio(); }
            @Override
            protected Component createPreviousButton() { return crearBotonVacio(); }

            private Component crearBotonVacio() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });

        if (spn.getEditor() instanceof JSpinner.DateEditor editor) {
            JFormattedTextField ftf = editor.getTextField();
            ftf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            ftf.setBorder(new EmptyBorder(4, 8, 4, 8));
            ftf.setBackground(COLOR_BG_CAMPO);
            ftf.setForeground(Color.WHITE);
            ftf.setCaretColor(Color.WHITE);

            spn.addPropertyChangeListener("enabled", evt -> {
                boolean en = spn.isEnabled();
                ftf.setBackground(en ? COLOR_BG_CAMPO : COLOR_BG_DESACTIVADO);
                ftf.setForeground(en ? Color.WHITE : new Color(100, 116, 139));
            });
        }

        spn.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        return spn;
    }

    private void configurarEstiloTabla(JTable tbl) {
        tbl.setBackground(COLOR_BG_CAMPO);
        tbl.setForeground(new Color(226, 232, 240));
        tbl.setSelectionBackground(COLOR_SELECCION_FILA);
        tbl.setSelectionForeground(Color.WHITE);
        tbl.setRowHeight(38);
        tbl.setShowGrid(false);

        if (tbl.getTableHeader() != null) {
            tbl.getTableHeader().setBackground(new Color(11, 15, 25));
            tbl.getTableHeader().setForeground(new Color(148, 163, 184));
            tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 10));
            tbl.getTableHeader().setPreferredSize(new Dimension(0, 32));
        }

        tbl.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setOpaque(true);

                if (isSelected) {
                    c.setBackground(COLOR_SELECCION_FILA);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? COLOR_BG_CAMPO : new Color(14, 19, 31));
                    c.setForeground(new Color(226, 232, 240));
                }
                c.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                c.setHorizontalAlignment(SwingConstants.LEFT);
                return c;
            }
        });
    }

    private void estilarScroll(JScrollBar scrollBar) {
        if (scrollBar == null) return;
        scrollBar.setBackground(COLOR_BG_CAMPO);
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(51, 65, 85);
                this.trackColor = COLOR_BG_CAMPO;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) { return crearBotonVacio(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return crearBotonVacio(); }

            private JButton crearBotonVacio() {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                return btn;
            }
        });
    }

    private void configurarFormulario() {
        for (TipoReporte reporte : TipoReporte.values()) cboReporte.addItem(reporte);

        cboEstado.addItem(TODOS);
        for (EstadoEquipo estado : EstadoEquipo.values()) cboEstado.addItem(estado);

        LocalDateTime ahora = LocalDateTime.now();
        SelectorFecha.configurar(spnDesde, ahora.minusMonths(1).withHour(0).withMinute(0));
        SelectorFecha.configurar(spnHasta, ahora.withHour(23).withMinute(59));

        tblReporte.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        habilitarFiltros();
    }

    private void configurarEventos() {
        cboReporte.addActionListener(e -> habilitarFiltros());
        btnConsultar.addActionListener(e -> consultar());
        txtCodigoEquipo.addActionListener(e -> consultar());
        btnSalir.addActionListener(e -> salir());
    }

    private void habilitarFiltros() {
        TipoReporte reporte = (TipoReporte) cboReporte.getSelectedItem();

        cboEstado.setEnabled(reporte == TipoReporte.EQUIPOS_POR_ESTADO);
        spnDesde.setEnabled(reporte == TipoReporte.PRESTAMOS_ENTRE_FECHAS);
        spnHasta.setEnabled(reporte == TipoReporte.PRESTAMOS_ENTRE_FECHAS);

        boolean codigoActivo = (reporte == TipoReporte.HISTORIAL_DE_EQUIPO
                || reporte == TipoReporte.HISTORIAL_DE_MANTENIMIENTO);
        txtCodigoEquipo.setEnabled(codigoActivo);

        cboEstado.repaint();
        spnDesde.repaint();
        spnHasta.repaint();
        txtCodigoEquipo.repaint();
    }

    private void consultar() {
        try {
            TipoReporte reporte = (TipoReporte) cboReporte.getSelectedItem();
            if (reporte == null) return;

            switch (reporte) {
                case EQUIPOS_POR_ESTADO -> mostrarEquipos(reporteController.equiposPorEstado(estadoSeleccionado()));
                case PRESTAMOS_ACTIVOS -> mostrarPrestamos(reporteController.prestamosActivos());
                case PRESTAMOS_FINALIZADOS -> mostrarPrestamos(reporteController.prestamosFinalizados());
                case PRESTAMOS_VENCIDOS -> mostrarPrestamos(reporteController.prestamosVencidos());
                case PRESTAMOS_ENTRE_FECHAS -> mostrarPrestamos(reporteController.prestamosEntreFechas(
                        SelectorFecha.leer(spnDesde, "inicio"), SelectorFecha.leer(spnHasta, "fin")));
                case HISTORIAL_DE_EQUIPO -> mostrarHistorial(reporteController.historialDeEquipo(txtCodigoEquipo.getText()));
                case HISTORIAL_DE_MANTENIMIENTO ->
                        mostrarMantenimientos(reporteController.historialMantenimiento(txtCodigoEquipo.getText()));
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private EstadoEquipo estadoSeleccionado() {
        return cboEstado.getSelectedItem() instanceof EstadoEquipo estado ? estado : null;
    }

    private void mostrarEquipos(List<Equipo> equipos) {
        TablaSoloLectura modelo = new TablaSoloLectura("Código", "Nombre", "Tipo", "Marca", "Modelo",
                "N° de serie", "Laboratorio", "Estado");

        for (Equipo equipo : equipos) {
            modelo.addRow(new Object[]{
                    equipo.getCodigoInventario(),
                    equipo.getNombre(),
                    equipo.getTipo(),
                    equipo.getMarca(),
                    equipo.getModelo(),
                    equipo.getNumeroSerie(),
                    equipo.getLaboratorio(),
                    equipo.getEstado()
            });
        }
        mostrar(modelo);

        tblReporte.getColumnModel().getColumn(0).setCellRenderer(new ChipCodigoDinamicoRenderer());
        tblReporte.getColumnModel().getColumn(7).setCellRenderer(new PillEstadoDinamicaRenderer());
    }

    private void mostrarPrestamos(List<PrestamoResumen> prestamos) {
        TablaSoloLectura modelo = new TablaSoloLectura("N°", "Solicitante", "Identificación", "Fecha préstamo",
                "Devolución esperada", "Registró", "Equipos", "Pendientes", "Estado", "Vencido");
        LocalDateTime ahora = LocalDateTime.now();

        for (PrestamoResumen resumen : prestamos) {
            Prestamo prestamo = resumen.prestamo();
            modelo.addRow(new Object[]{
                    prestamo.getIdPrestamo(),
                    prestamo.getSolicitanteNombre(),
                    prestamo.getSolicitanteIdentificacion(),
                    Fechas.texto(prestamo.getFechaPrestamo()),
                    Fechas.texto(prestamo.getFechaEsperadaDevolucion()),
                    prestamo.getUsuarioRegistra().getUsuario(),
                    resumen.totalEquipos(),
                    resumen.pendientes(),
                    prestamo.getEstado(),
                    resumen.vencido(ahora) ? "Sí" : "No"
            });
        }
        mostrar(modelo);

        tblReporte.getColumnModel().getColumn(0).setCellRenderer(new ChipCodigoDinamicoRenderer());
        tblReporte.getColumnModel().getColumn(8).setCellRenderer(new PillEstadoDinamicaRenderer());
    }

    private void mostrarHistorial(List<HistorialEquipo> historial) {
        TablaSoloLectura modelo = new TablaSoloLectura("N° de préstamo", "Solicitante", "Identificación",
                "Fecha préstamo", "Devolución esperada", "Fecha devolución", "Condición");

        for (HistorialEquipo fila : historial) {
            boolean pendiente = fila.fechaDevolucion() == null;
            modelo.addRow(new Object[]{
                    fila.idPrestamo(),
                    fila.solicitanteNombre(),
                    fila.solicitanteIdentificacion(),
                    Fechas.texto(fila.fechaPrestamo()),
                    Fechas.texto(fila.fechaEsperadaDevolucion()),
                    pendiente ? "PENDIENTE" : Fechas.texto(fila.fechaDevolucion()),
                    pendiente ? "" : fila.condicion()
            });
        }
        mostrar(modelo);

        tblReporte.getColumnModel().getColumn(0).setCellRenderer(new ChipCodigoDinamicoRenderer());
    }

    private void mostrarMantenimientos(List<Mantenimiento> mantenimientos) {
        TablaSoloLectura modelo = new TablaSoloLectura("N°", "Equipo", "Tipo", "Ingreso", "Responsable",
                "Descripción", "Estado", "Resultado", "Salida", "Observaciones");

        for (Mantenimiento mantenimiento : mantenimientos) {
            modelo.addRow(new Object[]{
                    mantenimiento.getIdMantenimiento(),
                    mantenimiento.getEquipo().getCodigoInventario() + " - " + mantenimiento.getEquipo().getNombre(),
                    mantenimiento.getTipo(),
                    Fechas.texto(mantenimiento.getFechaIngreso()),
                    mantenimiento.getResponsable(),
                    mantenimiento.getDescripcion(),
                    mantenimiento.getEstado(),
                    mantenimiento.getResultado() == null ? "" : mantenimiento.getResultado(),
                    Fechas.texto(mantenimiento.getFechaSalida()),
                    mantenimiento.getObservaciones()
            });
        }
        mostrar(modelo);

        tblReporte.getColumnModel().getColumn(0).setCellRenderer(new ChipCodigoDinamicoRenderer());
        tblReporte.getColumnModel().getColumn(6).setCellRenderer(new PillEstadoDinamicaRenderer());
    }

    private void mostrar(TablaSoloLectura modelo) {
        tblReporte.setModel(modelo);
        lblTotal.setText(modelo.getRowCount() + " registro(s)");
    }

    private void salir() {
        Window win = SwingUtilities.getWindowAncestor(panelPrincipal);
        if (win != null) {
            win.dispose();
        }
    }

    public JPanel getPanelPrincipal() {
        return panelPrincipal;
    }

    // ========================================================
    // RENDERIZADORES Y CLASES DE APOYO
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

    private static class ChipCodigoDinamicoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setHorizontalAlignment(SwingConstants.CENTER);

            if (isSelected) {
                label.setBackground(COLOR_SELECCION_FILA);
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(row % 2 == 0 ? COLOR_BG_CAMPO : new Color(14, 19, 31));
                Color colorBase = PALETA_COLORES[(row + 1) % PALETA_COLORES.length];
                label.setForeground(colorBase);
            }
            return label;
        }
    }

    private static class PillEstadoDinamicaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 10));

            String est = value != null ? value.toString().toUpperCase() : "-";
            label.setText(est);

            if (isSelected) {
                label.setBackground(COLOR_SELECCION_FILA);
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(row % 2 == 0 ? COLOR_BG_CAMPO : new Color(14, 19, 31));
                if (est.contains("DISPONIBLE") || est.contains("FINALIZADO") || est.contains("COMPLETADO")) {
                    label.setForeground(new Color(34, 197, 94));
                } else if (est.contains("PRESTADO") || est.contains("PROCESO") || est.contains("MANTENIMIENTO")) {
                    label.setForeground(new Color(245, 158, 11));
                } else {
                    label.setForeground(new Color(239, 68, 68));
                }
            }
            return label;
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

            g2.setColor(COLOR_BORDER);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius);

            g2.dispose();
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
            setFont(new Font("Segoe UI", Font.BOLD, 11));
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

    private static class IconoReportes implements Icon {
        private final Color color;

        public IconoReportes(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2.drawLine(x + 4, y + 20, x + 4, y + 14);
            g2.drawLine(x + 10, y + 20, x + 10, y + 8);
            g2.drawLine(x + 16, y + 20, x + 16, y + 11);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 22; }

        @Override
        public int getIconHeight() { return 22; }
    }
}