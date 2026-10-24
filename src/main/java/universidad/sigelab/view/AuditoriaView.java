package universidad.sigelab.view;

import universidad.sigelab.controller.AuditoriaController;
import universidad.sigelab.enums.AccionAuditoria;
import universidad.sigelab.model.Auditoria;
import universidad.sigelab.model.Usuario;
import universidad.sigelab.util.Fechas;

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
import java.util.List;

public class AuditoriaView {

    private static final String TODAS = "TODAS";

    private JPanel panelPrincipal;

    private JComboBox<Object> cboAccion;
    private JButton btnConsultar;
    private JLabel lblTotal;

    private JTable tblAuditoria;
    private JScrollPane scrollTabla;
    private JButton btnSalir;

    // Métricas KPI
    private JLabel lblKpiTotalEventos;
    private JLabel lblKpiUltimoAcceso;
    private JLabel lblKpiUltimoUsuario;
    private JLabel lblKpiExitosos;
    private JLabel lblKpiExitososPct;
    private JLabel lblKpiErrores;
    private JLabel lblFooterStats;

    private Usuario usuarioActual;
    private final AuditoriaController auditoriaController;

    public AuditoriaView() {
        this(null, new AuditoriaController());
    }

    public AuditoriaView(Usuario usuario) {
        this(usuario, new AuditoriaController());
    }

    public AuditoriaView(AuditoriaController auditoriaController) {
        this(null, auditoriaController);
    }

    public AuditoriaView(Usuario usuario, AuditoriaController auditoriaController) {
        this.usuarioActual = usuario;
        this.auditoriaController = auditoriaController;

        initComponents();
        configurarFormulario();
        configurarEventos();
        consultar();
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
                g2.setColor(new Color(244, 63, 94));
                g2.fillRect(0, 0, 4, getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(4, 12, 4, 12));

        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBox.setOpaque(false);

        JLabel lblTitle = new JLabel("AUDITORÍA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblIcono = new JLabel(new IconoLupaAuditoria(new Color(244, 63, 94)));

        JPanel titleIconContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleIconContainer.setOpaque(false);
        titleIconContainer.add(lblTitle);
        titleIconContainer.add(lblIcono);

        JLabel lblSub = new JLabel("Bitácora de trazabilidad");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184));

        JPanel textGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        textGroup.setOpaque(false);
        textGroup.add(titleIconContainer);
        textGroup.add(lblSub);

        leftBox.add(textGroup);

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
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 8, 0));
        kpiGrid.setOpaque(false);

        lblKpiTotalEventos = new JLabel("0");
        lblKpiUltimoAcceso = new JLabel("-");
        lblKpiUltimoUsuario = new JLabel("(-)");
        lblKpiExitosos = new JLabel("0");
        lblKpiExitososPct = new JLabel("(0%)");
        lblKpiErrores = new JLabel("0");

        kpiGrid.add(crearCardKpiSimple("TOTAL EVENTOS", lblKpiTotalEventos, "evento(s)", "📄", new Color(14, 116, 144)));
        kpiGrid.add(crearCardKpiDoble("ÚLTIMO ACCESO", lblKpiUltimoAcceso, lblKpiUltimoUsuario, "🕒", new Color(30, 58, 138)));
        kpiGrid.add(crearCardKpiDoble("INGRESOS EXITOSOS", lblKpiExitosos, lblKpiExitososPct, "✅", new Color(16, 185, 129)));
        kpiGrid.add(crearCardKpiSimple("ERRORES TOTALES", lblKpiErrores, "fallo(s)", "⚠️", new Color(239, 68, 68)));

        return kpiGrid;
    }

    private JPanel crearCardKpiSimple(String titulo, JLabel lblValor, String subtexto, String icono, Color accentColor) {
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

    private JPanel crearCardKpiDoble(String titulo, JLabel lblValor1, JLabel lblValor2, String icono, Color accentColor) {
        CardPanel card = new CardPanel(12, new Color(18, 24, 38));
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(8, 10, 8, 10));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel(titulo);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblTitle.setForeground(new Color(148, 163, 184));

        lblValor1.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblValor1.setForeground(Color.WHITE);

        lblValor2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblValor2.setForeground(new Color(56, 189, 248));

        left.add(lblTitle);
        left.add(Box.createVerticalStrut(2));
        left.add(lblValor1);
        left.add(Box.createVerticalStrut(1));
        left.add(lblValor2);

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

    private JPanel crearCardTabla() {
        CardPanel card = new CardPanel(14, new Color(18, 24, 38));
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel filterBar = new JPanel(new BorderLayout(10, 0));
        filterBar.setOpaque(false);

        JPanel filterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterLeft.setOpaque(false);

        JLabel lblAccionLabel = new JLabel("Acción");
        lblAccionLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblAccionLabel.setForeground(new Color(226, 232, 240));

        cboAccion = crearCombo();
        cboAccion.setPreferredSize(new Dimension(280, 32));

        btnConsultar = new BotonEstilizado("Consultar", Color.WHITE, new Color(15, 23, 42), new Color(255, 255, 255, 220));
        btnConsultar.setPreferredSize(new Dimension(110, 32));

        filterLeft.add(lblAccionLabel);
        filterLeft.add(cboAccion);
        filterLeft.add(btnConsultar);

        lblTotal = new JLabel("0 evento(s)");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotal.setForeground(new Color(56, 189, 248));
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel rightWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightWrapper.setOpaque(false);
        rightWrapper.add(lblTotal);

        filterBar.add(filterLeft, BorderLayout.WEST);
        filterBar.add(rightWrapper, BorderLayout.EAST);

        tblAuditoria = new JTable();
        configurarTabla();

        scrollTabla = new JScrollPane(tblAuditoria);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());
        scrollTabla.setBackground(new Color(11, 15, 25));
        scrollTabla.getViewport().setBackground(new Color(18, 24, 38));

        // SOLUCIÓN DEFINITIVA A LA BARRA/ESQUINA BLANCA MEDIANTE DIBUJO DIRECTO
        scrollTabla.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new EsquinaTablaOscura(new Color(11, 15, 25)));

        scrollTabla.addMouseWheelListener(e -> {
            JScrollBar vertical = scrollTabla.getVerticalScrollBar();
            if (vertical != null) {
                int amount = e.getWheelRotation() * 24;
                vertical.setValue(vertical.getValue() + amount);
            }
        });

        estilarScroll(scrollTabla.getVerticalScrollBar());

        card.add(filterBar, BorderLayout.NORTH);
        card.add(scrollTabla, BorderLayout.CENTER);

        return card;
    }

    private JPanel crearFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        lblFooterStats = new JLabel("● Total: 0 eventos registrados | Mostrando registros recientes de auditoría de seguridad");
        lblFooterStats.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblFooterStats.setForeground(new Color(56, 189, 248));

        btnSalir = new BotonEstilizado("➔ Salir", new Color(18, 24, 38), Color.WHITE, new Color(46, 59, 82, 180));
        btnSalir.setPreferredSize(new Dimension(100, 32));

        footer.add(lblFooterStats, BorderLayout.WEST);
        footer.add(btnSalir, BorderLayout.EAST);

        return footer;
    }

    private <T> JComboBox<T> crearCombo() {
        JComboBox<T> cb = new JComboBox<>();
        cb.setBackground(new Color(15, 23, 42));
        cb.setForeground(Color.WHITE);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        cb.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("▼");
                btn.setFont(new Font("Segoe UI", Font.BOLD, 9));
                btn.setForeground(new Color(148, 163, 184));
                btn.setBackground(new Color(30, 41, 59));
                btn.setBorder(BorderFactory.createLineBorder(new Color(46, 59, 82)));
                btn.setFocusable(false);
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(new Color(15, 23, 42));
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });

        cb.setBorder(new LineBorder(new Color(46, 59, 82), 1, true));

        cb.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                cb.setBorder(new LineBorder(new Color(59, 130, 246), 2, true));
            }

            @Override
            public void focusLost(FocusEvent e) {
                cb.setBorder(new LineBorder(new Color(46, 59, 82), 1, true));
            }
        });

        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setOpaque(true);
                l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                l.setBorder(new EmptyBorder(6, 10, 6, 10));

                if (isSelected && index >= 0) {
                    l.setBackground(new Color(30, 58, 138));
                    l.setForeground(Color.WHITE);
                } else {
                    l.setBackground(new Color(15, 23, 42));
                    l.setForeground(Color.WHITE);
                }
                return l;
            }
        });
        return cb;
    }

    private void configurarTabla() {
        tblAuditoria.setModel(new TablaSoloLectura("Fecha y hora", "Usuario", "Acción", "Entidad",
                "ID del registro", "Descripción"));
        tblAuditoria.setBackground(new Color(18, 24, 38));
        tblAuditoria.setForeground(new Color(226, 232, 240));
        tblAuditoria.setSelectionBackground(new Color(30, 41, 59));
        tblAuditoria.setSelectionForeground(Color.WHITE);
        tblAuditoria.setRowHeight(38);
        tblAuditoria.setShowGrid(false);
        tblAuditoria.setFillsViewportHeight(true);
        tblAuditoria.setIntercellSpacing(new Dimension(0, 0));

        // RENDERER PERSONALIZADO DEL HEADER CON BORDE MATTE
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

        if (tblAuditoria.getTableHeader() != null) {
            tblAuditoria.getTableHeader().setBackground(new Color(11, 15, 25));
            tblAuditoria.getTableHeader().setOpaque(true);
            tblAuditoria.getTableHeader().setPreferredSize(new Dimension(0, 32));
            tblAuditoria.getTableHeader().setDefaultRenderer(headerRenderer);
        }

        tblAuditoria.getColumnModel().getColumn(0).setCellRenderer(new FechaRenderer());
        tblAuditoria.getColumnModel().getColumn(2).setCellRenderer(new AccionPillRenderer());
        tblAuditoria.getColumnModel().getColumn(4).setCellRenderer(new ChipIdRenderer());
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

    private void configurarFormulario() {
        cboAccion.addItem(TODAS);
        for (AccionAuditoria accion : AccionAuditoria.values()) cboAccion.addItem(accion);

        tblAuditoria.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void configurarEventos() {
        btnConsultar.addActionListener(e -> consultar());
        cboAccion.addActionListener(e -> consultar());
        btnSalir.addActionListener(e -> salir());
    }

    private void consultar() {
        try {
            Object seleccion = cboAccion.getSelectedItem();
            AccionAuditoria accion = seleccion instanceof AccionAuditoria elegida ? elegida : null;

            List<Auditoria> eventos = auditoriaController.listar(accion);

            TablaSoloLectura modelo = new TablaSoloLectura("Fecha y hora", "Usuario", "Acción", "Entidad",
                    "ID del registro", "Descripción");

            int exitosos = 0;
            int errores = 0;
            String ultimoFecha = "-";
            String ultimoUser = "(-)";

            if (!eventos.isEmpty()) {
                Auditoria reciente = eventos.get(0);
                ultimoFecha = Fechas.texto(reciente.getFechaHora());
                ultimoUser = reciente.getUsuario() != null ? "(" + reciente.getUsuario().getUsuario() + ")" : "(desconocido)";
            }

            for (Auditoria evento : eventos) {
                String nomUsuario = evento.getUsuario() == null ? "(desconocido)" : evento.getUsuario().getUsuario();
                String accionTexto = evento.getAccion() != null ? evento.getAccion().toString() : "";

                modelo.addRow(new Object[]{
                        Fechas.texto(evento.getFechaHora()),
                        nomUsuario,
                        evento.getAccion(),
                        evento.getEntidad(),
                        evento.getIdRegistro() == null ? "" : evento.getIdRegistro(),
                        evento.getDescripcion()
                });

                if (accionTexto.contains("EXITOSO") || accionTexto.contains("INSERTAR") || accionTexto.contains("ACTUALIZAR")) {
                    exitosos++;
                } else if (accionTexto.contains("FALLIDO") || accionTexto.contains("ERROR") || accionTexto.contains("ELIMINAR")) {
                    errores++;
                } else {
                    exitosos++;
                }
            }

            tblAuditoria.setModel(modelo);

            int total = eventos.size();
            lblTotal.setText(total + " evento(s)");

            lblKpiTotalEventos.setText(String.valueOf(total));
            lblKpiUltimoAcceso.setText(ultimoFecha);
            lblKpiUltimoUsuario.setText(ultimoUser);
            lblKpiExitosos.setText(String.valueOf(exitosos));

            int pct = total > 0 ? (int) Math.round(((double) exitosos / total) * 100) : 0;
            lblKpiExitososPct.setText(exitosos + " (" + pct + "%)");
            lblKpiErrores.setText(String.valueOf(errores));

            lblFooterStats.setText("● Total: " + total + " eventos registrados | Mostrando registros recientes de auditoría de seguridad");

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
            tblAuditoria.getTableHeader().setDefaultRenderer(headerRenderer);

            tblAuditoria.getColumnModel().getColumn(0).setCellRenderer(new FechaRenderer());
            tblAuditoria.getColumnModel().getColumn(2).setCellRenderer(new AccionPillRenderer());
            tblAuditoria.getColumnModel().getColumn(4).setCellRenderer(new ChipIdRenderer());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
    // PANEL PERSONALIZADO PARA PINTAR LA ESQUINA DEL SCROLL
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

    private static class IconoLupaAuditoria implements Icon {
        private final Color color;

        public IconoLupaAuditoria(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 35));
            g2.fillOval(x, y, 32, 32);

            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2.drawOval(x + 8, y + 8, 12, 12);
            g2.drawLine(x + 18, y + 18, x + 24, y + 24);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 32; }

        @Override
        public int getIconHeight() { return 32; }
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
            setBorder(new EmptyBorder(7, 12, 7, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(glowColor);
            g2.fillRoundRect(1, 2, w - 2, h - 2, 10, 10);

            g2.setColor(getModel().isPressed() ? bgColor.darker() : bgColor);
            g2.fillRoundRect(0, 0, w - 1, h - 2, 10, 10);

            g2.setColor(new Color(255, 255, 255, 30));
            g2.drawRoundRect(0, 0, w - 2, h - 3, 10, 10);

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

    private static class FechaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(new Color(52, 211, 153));
            label.setBackground(isSelected ? new Color(30, 41, 59) : (row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31)));
            return label;
        }
    }

    private static class ChipIdRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(false);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(new Color(56, 189, 248));

            JPanel panel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth() - 24;
                    int h = getHeight() - 8;
                    int x = (getWidth() - w) / 2;
                    int y = (getHeight() - h) / 2;

                    g2.setColor(new Color(30, 58, 138, 200));
                    g2.fillRoundRect(x, y, w, h, 10, 10);
                    g2.dispose();
                }
            };

            panel.setOpaque(true);
            panel.setBackground(isSelected ? new Color(30, 41, 59) : (row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31)));
            panel.add(label, BorderLayout.CENTER);
            return panel;
        }
    }

    private static class AccionPillRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(false);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 10));

            String act = value != null ? value.toString().toUpperCase() : "";

            JPanel panel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    boolean fallo = act.contains("FALLIDO") || act.contains("ERROR");
                    Color bgPill = fallo ? new Color(110, 30, 15) : new Color(12, 74, 96);
                    Color glowBorder = fallo ? new Color(239, 68, 68) : new Color(56, 189, 248);

                    int w = getWidth() - 14;
                    int h = getHeight() - 8;
                    int x = (getWidth() - w) / 2;
                    int y = (getHeight() - h) / 2;

                    g2.setColor(bgPill);
                    g2.fillRoundRect(x, y, w, h, 8, 8);

                    g2.setColor(glowBorder);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(x, y, w - 1, h - 1, 8, 8);

                    g2.dispose();
                }
            };

            panel.setOpaque(true);
            panel.setBackground(isSelected ? new Color(30, 41, 59) : (row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31)));

            boolean fallo = act.contains("FALLIDO") || act.contains("ERROR");
            label.setText(act);
            label.setForeground(fallo ? new Color(248, 113, 113) : new Color(56, 189, 248));

            panel.add(label, BorderLayout.CENTER);
            return panel;
        }
    }
}