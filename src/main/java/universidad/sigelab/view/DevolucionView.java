package universidad.sigelab.view;

import universidad.sigelab.controller.DevolucionController;
import universidad.sigelab.enums.CondicionDevolucion;
import universidad.sigelab.enums.EstadoPrestamo;
import universidad.sigelab.model.DetallePrestamo;
import universidad.sigelab.model.Devolucion;
import universidad.sigelab.model.Prestamo;
import universidad.sigelab.model.PrestamoResumen;
import universidad.sigelab.model.Usuario;
import universidad.sigelab.util.BusinessException;
import universidad.sigelab.util.Fechas;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class DevolucionView {

    private JPanel panelPrincipal;

    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JButton btnVerActivos;

    private JTable tblPrestamos;
    private JTable tblEquipos;

    private JComboBox<CondicionDevolucion> cboCondicion;
    private JTextField txtObservacion;
    private JButton btnDevolver;

    private JButton btnSalir;
    private JLabel lblContadorResultados;

    private Usuario usuarioActual;
    private final DevolucionController devolucionController;

    private static final Color[] PALETA_COLORES = {
            new Color(56, 189, 248),  // Cian
            new Color(16, 185, 129),  // Esmeralda
            new Color(245, 158, 11),  // Ámbar
            new Color(139, 92, 246),  // Púrpura
            new Color(236, 72, 153),  // Rosa
            new Color(20, 184, 166),  // Teal
            new Color(99, 102, 241)   // Índigo
    };

    public DevolucionView() {
        this(null, new DevolucionController());
    }

    public DevolucionView(Usuario usuario) {
        this(usuario, new DevolucionController());
    }

    public DevolucionView(DevolucionController devolucionController) {
        this(null, devolucionController);
    }

    public DevolucionView(Usuario usuario, DevolucionController devolucionController) {
        this.usuarioActual = usuario;
        this.devolucionController = devolucionController;

        initComponents();
        configurarEventos();
        cargarPrestamosActivos();
    }

    private void initComponents() {
        panelPrincipal = new JPanel(new BorderLayout(0, 12));
        panelPrincipal.setBackground(new Color(11, 15, 25));
        panelPrincipal.setBorder(new EmptyBorder(16, 20, 16, 20));

        panelPrincipal.add(crearHeader(), BorderLayout.NORTH);

        JPanel centroPanel = new JPanel();
        centroPanel.setLayout(new BoxLayout(centroPanel, BoxLayout.Y_AXIS));
        centroPanel.setOpaque(false);

        centroPanel.add(crearBarraBusqueda());
        centroPanel.add(Box.createVerticalStrut(10));
        centroPanel.add(crearCardPrestamosEncontrados());
        centroPanel.add(Box.createVerticalStrut(10));
        centroPanel.add(crearCardEquiposDelPrestamo());
        centroPanel.add(Box.createVerticalStrut(10));
        centroPanel.add(crearCardDevolverEquipo());

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
                g2.setColor(new Color(16, 185, 129));
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

        JLabel lblTitle = new JLabel("DEVOLUCIONES");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Ingreso y recepción de préstamos");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184));

        textGroup.add(lblTitle);
        textGroup.add(lblSub);

        JLabel lblIconoRing = new JLabel(new IconoAnilloVerde());

        leftBox.add(textGroup);
        leftBox.add(lblIconoRing);

        header.add(leftBox, BorderLayout.WEST);
        header.add(crearBadgeUsuarioDinamico(usuarioActual), BorderLayout.EAST);

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

    private JPanel crearBarraBusqueda() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);

        JPanel inputGroup = new JPanel(new BorderLayout(8, 0));
        inputGroup.setBackground(new Color(18, 24, 38));
        inputGroup.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(46, 59, 82), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JLabel lblLeading = new JLabel(" #  N° DE PRÉSTAMO   ");
        lblLeading.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblLeading.setForeground(new Color(56, 189, 248));

        txtBuscar = new JTextField();
        txtBuscar.setBackground(new Color(18, 24, 38));
        txtBuscar.setForeground(Color.WHITE);
        txtBuscar.setCaretColor(Color.WHITE);
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtBuscar.setBorder(null);

        JLabel lblTrailing = new JLabel(" ID / SCAN ");
        lblTrailing.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblTrailing.setForeground(new Color(100, 116, 139));

        inputGroup.add(lblLeading, BorderLayout.WEST);
        inputGroup.add(txtBuscar, BorderLayout.CENTER);
        inputGroup.add(lblTrailing, BorderLayout.EAST);

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBox.setOpaque(false);

        btnBuscar = new BotonEstilizado("🔍 Buscar", new Color(30, 58, 138), Color.WHITE, new Color(59, 130, 246, 180));
        btnVerActivos = new BotonEstilizado("☑ Ver préstamos activos", new Color(18, 24, 38), new Color(148, 163, 184), new Color(46, 59, 82, 180));

        btnBox.add(btnBuscar);
        btnBox.add(btnVerActivos);

        bar.add(inputGroup, BorderLayout.CENTER);
        bar.add(btnBox, BorderLayout.EAST);

        return bar;
    }

    private JPanel crearCardPrestamosEncontrados() {
        CardPanel card = new CardPanel(14, new Color(18, 24, 38));
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("● PRÉSTAMOS ENCONTRADOS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(96, 165, 250));

        lblContadorResultados = new JLabel("0 resultados cargados");
        lblContadorResultados.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblContadorResultados.setForeground(new Color(100, 116, 139));

        topHeader.add(lblTitle, BorderLayout.WEST);
        topHeader.add(lblContadorResultados, BorderLayout.EAST);

        tblPrestamos = new JTable();
        configurarEstiloTabla(tblPrestamos);

        tblPrestamos.setModel(new TablaSoloLectura("N°", "SOLICITANTE", "IDENTIFICACIÓN", "FECHA PRÉSTAMO",
                "DEVOLUCIÓN ESPERADA", "EQUIPOS", "PENDIENTES", "ESTADO", "VENCIDO"));
        tblPrestamos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblPrestamos.getColumnModel().getColumn(0).setCellRenderer(new BadgeIdRenderer());
        tblPrestamos.getColumnModel().getColumn(1).setCellRenderer(new AvatarNombreRenderer());
        tblPrestamos.getColumnModel().getColumn(7).setCellRenderer(new ChipEstadoPrestamoRenderer());
        tblPrestamos.getColumnModel().getColumn(8).setCellRenderer(new ChipVencidoRenderer());

        JScrollPane scroll = new JScrollPane(tblPrestamos);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(new Color(11, 15, 25));
        scroll.getViewport().setBackground(new Color(18, 24, 38));
        scroll.setPreferredSize(new Dimension(800, 120));

        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new EsquinaTablaOscura(new Color(11, 15, 25)));
        estilarScroll(scroll.getVerticalScrollBar());

        card.add(topHeader, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel crearCardEquiposDelPrestamo() {
        CardPanel card = new CardPanel(14, new Color(18, 24, 38));
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("● EQUIPOS DEL PRÉSTAMO SELECCIONADO");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(96, 165, 250));

        JLabel lblHint = new JLabel("Seleccione un equipo de la lista para registrar su devolución");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHint.setForeground(new Color(100, 116, 139));

        topHeader.add(lblTitle, BorderLayout.WEST);
        topHeader.add(lblHint, BorderLayout.EAST);

        tblEquipos = new JTable();
        configurarEstiloTabla(tblEquipos);

        tblEquipos.setModel(new TablaSoloLectura("N° DETALLE", "CÓDIGO", "NOMBRE", "DEVOLUCIÓN", "FECHA DEVOLUCIÓN",
                "OBSERVACIÓN", "ESTADO DEL EQUIPO"));
        tblEquipos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblEquipos.getColumnModel().getColumn(1).setCellRenderer(new ChipCodigoEquipoRenderer());
        tblEquipos.getColumnModel().getColumn(3).setCellRenderer(new ChipEstadoDevolucionRenderer());
        tblEquipos.getColumnModel().getColumn(6).setCellRenderer(new ChipEstadoEquipoRenderer());

        JScrollPane scroll = new JScrollPane(tblEquipos);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(new Color(11, 15, 25));
        scroll.getViewport().setBackground(new Color(18, 24, 38));
        scroll.setPreferredSize(new Dimension(800, 140));

        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new EsquinaTablaOscura(new Color(11, 15, 25)));
        estilarScroll(scroll.getVerticalScrollBar());

        card.add(topHeader, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel crearCardDevolverEquipo() {
        CardPanel card = new CardPanel(14, new Color(18, 24, 38));
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel lblTitle = new JLabel("↶ DEVOLVER EL EQUIPO SELECCIONADO");
        lblTitle.setFont(new Font("Segoe UI Symbol", Font.BOLD, 11));
        lblTitle.setForeground(new Color(56, 189, 248));

        JPanel body = new JPanel(new BorderLayout(14, 0));
        body.setOpaque(false);

        JPanel groupCombo = new JPanel(new BorderLayout(0, 4));
        groupCombo.setOpaque(false);
        groupCombo.setPreferredSize(new Dimension(200, 50));

        JLabel lblC = new JLabel("CONDICIÓN EN QUE REGRESA");
        lblC.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblC.setForeground(new Color(148, 163, 184));

        cboCondicion = new JComboBox<>();
        cboCondicion.setBackground(new Color(18, 24, 38));
        cboCondicion.setForeground(Color.WHITE);
        cboCondicion.setFont(new Font("Segoe UI", Font.BOLD, 12));

        cboCondicion.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setOpaque(true);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setBorder(new EmptyBorder(6, 10, 6, 10));

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

        for (CondicionDevolucion condicion : CondicionDevolucion.values()) {
            cboCondicion.addItem(condicion);
        }

        groupCombo.add(lblC, BorderLayout.NORTH);
        groupCombo.add(cboCondicion, BorderLayout.CENTER);

        JPanel groupObs = new JPanel(new BorderLayout(0, 4));
        groupObs.setOpaque(false);

        JLabel lblObs = new JLabel("OBSERVACIÓN DE ENTREGA");
        lblObs.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblObs.setForeground(new Color(148, 163, 184));

        txtObservacion = new JTextField();
        txtObservacion.setBackground(new Color(18, 24, 38));
        txtObservacion.setForeground(Color.WHITE);
        txtObservacion.setCaretColor(Color.WHITE);
        txtObservacion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtObservacion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(46, 59, 82), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));

        groupObs.add(lblObs, BorderLayout.NORTH);
        groupObs.add(txtObservacion, BorderLayout.CENTER);

        btnDevolver = new BotonEstilizado("✓ REGISTRAR DEVOLUCIÓN", new Color(248, 250, 252), new Color(15, 23, 42), new Color(255, 255, 255, 220));
        btnDevolver.setPreferredSize(new Dimension(210, 38));

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 14));
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnDevolver);

        body.add(groupCombo, BorderLayout.WEST);
        body.add(groupObs, BorderLayout.CENTER);
        body.add(btnWrapper, BorderLayout.EAST);

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

    private void configurarEstiloTabla(JTable tbl) {
        tbl.setBackground(new Color(18, 24, 38));
        tbl.setForeground(new Color(226, 232, 240));
        tbl.setSelectionBackground(new Color(30, 41, 59));
        tbl.setSelectionForeground(Color.WHITE);
        tbl.setRowHeight(40);
        tbl.setShowGrid(false);
        tbl.setIntercellSpacing(new Dimension(0, 0));

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

        if (tbl.getTableHeader() != null) {
            tbl.getTableHeader().setBackground(new Color(11, 15, 25));
            tbl.getTableHeader().setOpaque(true);
            tbl.getTableHeader().setPreferredSize(new Dimension(0, 32));
            tbl.getTableHeader().setDefaultRenderer(headerRenderer);
        }

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
        tbl.setDefaultRenderer(Object.class, defaultRenderer);
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
        btnBuscar.addActionListener(e -> buscar());
        btnVerActivos.addActionListener(e -> cargarPrestamosActivos());
        btnDevolver.addActionListener(e -> devolver());
        btnSalir.addActionListener(e -> salir());

        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    buscar();
                }
            }
        });

        tblPrestamos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarEquiposDelPrestamo();
        });
    }

    private void cargarPrestamosActivos() {
        try {
            txtBuscar.setText("");
            mostrarPrestamos(devolucionController.listarPrestamosActivos());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscar() {
        if (txtBuscar.getText().isBlank()) {
            cargarPrestamosActivos();
            return;
        }

        try {
            int idPrestamo = leerNumeroDePrestamo();
            Optional<PrestamoResumen> resultado = devolucionController.buscarPrestamo(idPrestamo);

            if (resultado.isPresent()) {
                mostrarPrestamos(List.of(resultado.get()));
                seleccionarPrestamo(idPrestamo);
            } else {
                JOptionPane.showMessageDialog(panelPrincipal, "No existe el préstamo N° " + idPrestamo + ".", "Búsqueda", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarPrestamos(List<PrestamoResumen> prestamos) {
        DefaultTableModel modelo = (DefaultTableModel) tblPrestamos.getModel();
        modelo.setRowCount(0);
        LocalDateTime ahora = LocalDateTime.now();

        for (PrestamoResumen resumen : prestamos) {
            Prestamo prestamo = resumen.prestamo();
            modelo.addRow(new Object[]{
                    prestamo.getIdPrestamo(),
                    prestamo.getSolicitanteNombre(),
                    prestamo.getSolicitanteIdentificacion(),
                    Fechas.texto(prestamo.getFechaPrestamo()),
                    Fechas.texto(prestamo.getFechaEsperadaDevolucion()),
                    resumen.totalEquipos(),
                    resumen.pendientes(),
                    prestamo.getEstado(),
                    resumen.vencido(ahora) ? "Sí" : "No"
            });
        }

        if (lblContadorResultados != null) {
            lblContadorResultados.setText(prestamos.size() + " resultado" + (prestamos.size() != 1 ? "s" : "") + " cargado" + (prestamos.size() != 1 ? "s" : ""));
        }

        ((DefaultTableModel) tblEquipos.getModel()).setRowCount(0);
    }

    private void seleccionarPrestamo(int idPrestamo) {
        for (int fila = 0; fila < tblPrestamos.getRowCount(); fila++) {
            if ((int) tblPrestamos.getValueAt(fila, 0) == idPrestamo) {
                tblPrestamos.setRowSelectionInterval(fila, fila);
                tblPrestamos.scrollRectToVisible(tblPrestamos.getCellRect(fila, 0, true));
                return;
            }
        }
    }

    private void cargarEquiposDelPrestamo() {
        DefaultTableModel modelo = (DefaultTableModel) tblEquipos.getModel();
        modelo.setRowCount(0);

        int fila = tblPrestamos.getSelectedRow();
        if (fila == -1) return;

        try {
            int idPrestamo = (int) tblPrestamos.getValueAt(fila, 0);

            for (DetallePrestamo detalle : devolucionController.listarEquipos(idPrestamo)) {
                Devolucion devolucion = detalle.getDevolucion();
                modelo.addRow(new Object[]{
                        detalle.getIdDetallePrestamo(),
                        detalle.getEquipo().getCodigoInventario(),
                        detalle.getEquipo().getNombre(),
                        detalle.isPendiente() ? "PENDIENTE" : "DEVUELTO (" + devolucion.getCondicion() + ")",
                        detalle.isPendiente() ? "—" : Fechas.texto(devolucion.getFechaDevolucion()),
                        detalle.isPendiente() ? "—" : devolucion.getObservacion(),
                        detalle.getEquipo().getEstado()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void devolver() {
        int filaPrestamo = tblPrestamos.getSelectedRow();
        int filaEquipo = tblEquipos.getSelectedRow();

        if (filaPrestamo == -1 || filaEquipo == -1) {
            JOptionPane.showMessageDialog(panelPrincipal, "Seleccione un préstamo y el equipo que se devuelve.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int idPrestamo = (int) tblPrestamos.getValueAt(filaPrestamo, 0);
            int idDetalle = (int) tblEquipos.getValueAt(filaEquipo, 0);
            String codigo = (String) tblEquipos.getValueAt(filaEquipo, 1);

            Devolucion devolucion = devolucionController.registrarDevolucion(idDetalle,
                    (CondicionDevolucion) cboCondicion.getSelectedItem(), txtObservacion.getText());

            boolean finalizado = devolucionController.buscarPrestamo(idPrestamo)
                    .map(resumen -> resumen.prestamo().getEstado() == EstadoPrestamo.FINALIZADO)
                    .orElse(false);

            JOptionPane.showMessageDialog(panelPrincipal, "Devolución registrada: " + codigo + " en condición " +
                    devolucion.getCondicion() + ".\n" + (finalizado
                    ? "El préstamo N° " + idPrestamo + " quedó FINALIZADO: ya no tiene equipos pendientes."
                    : "El préstamo N° " + idPrestamo + " sigue ACTIVO: todavía tiene equipos pendientes."), "Devolución", JOptionPane.INFORMATION_MESSAGE);

            txtObservacion.setText("");
            recargar(idPrestamo, finalizado);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recargar(int idPrestamo, boolean finalizado) {
        if (finalizado) {
            devolucionController.buscarPrestamo(idPrestamo).ifPresent(resumen -> mostrarPrestamos(List.of(resumen)));
        } else {
            mostrarPrestamos(devolucionController.listarPrestamosActivos());
        }
        seleccionarPrestamo(idPrestamo);
    }

    private int leerNumeroDePrestamo() {
        try {
            return Integer.parseInt(txtBuscar.getText().trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("El número de préstamo debe ser un número entero.");
        }
    }

    private void salir() {
        Window window = SwingUtilities.getWindowAncestor(panelPrincipal);
        if (window != null) {
            window.dispose();
        }
    }

    public JPanel getPanelPrincipal() {
        return panelPrincipal;
    }

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

    private static class IconoAnilloVerde implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = 32;
            int cx = x + size / 2;
            int cy = y + size / 2;

            g2.setColor(new Color(15, 35, 30));
            g2.fillOval(x, y, size, size);

            Color verdeGlow = new Color(16, 185, 129);
            g2.setColor(verdeGlow);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2.drawArc(cx - 8, cy - 8, 16, 16, 40, 280);

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
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));

            Color colorBase = PALETA_COLORES[row % PALETA_COLORES.length];
            label.setForeground(colorBase);

            JPanel panel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth() - 20;
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

    private static class AvatarNombreRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(Color.WHITE);
            label.setHorizontalAlignment(SwingConstants.LEFT);

            String text = value != null ? value.toString() : "";
            label.setText("  👤 " + text);

            if (isSelected) {
                label.setBackground(new Color(30, 41, 59));
            } else {
                label.setBackground(row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31));
            }

            return label;
        }
    }

    private static class ChipEstadoPrestamoRenderer extends DefaultTableCellRenderer {
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

                    boolean activo = st.contains("ACTIVO");
                    Color bg = activo ? new Color(12, 74, 96) : new Color(30, 41, 59);
                    Color border = activo ? new Color(56, 189, 248) : new Color(148, 163, 184);

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
            label.setForeground(st.contains("ACTIVO") ? new Color(56, 189, 248) : new Color(148, 163, 184));

            panel.add(label, BorderLayout.CENTER);
            return panel;
        }
    }

    private static class ChipVencidoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(false);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 10));

            boolean esSi = value != null && value.toString().equalsIgnoreCase("Sí");

            JPanel panel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    Color bg = esSi ? new Color(110, 30, 15) : new Color(6, 78, 59);
                    Color border = esSi ? new Color(239, 68, 68) : new Color(52, 211, 153);

                    int w = getWidth() - 16;
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

            label.setText(esSi ? "● VENCIDO" : "● AL DÍA");
            label.setForeground(esSi ? new Color(248, 113, 113) : new Color(52, 211, 153));

            panel.add(label, BorderLayout.CENTER);
            return panel;
        }
    }

    private static class ChipCodigoEquipoRenderer extends DefaultTableCellRenderer {
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

    private static class ChipEstadoDevolucionRenderer extends DefaultTableCellRenderer {
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

                    boolean pendiente = st.contains("PENDIENTE");
                    Color bg = pendiente ? new Color(110, 30, 15) : new Color(6, 78, 59);
                    Color border = pendiente ? new Color(239, 68, 68) : new Color(52, 211, 153);

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
            label.setForeground(st.contains("PENDIENTE") ? new Color(248, 113, 113) : new Color(52, 211, 153));

            panel.add(label, BorderLayout.CENTER);
            return panel;
        }
    }

    private static class ChipEstadoEquipoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(new Color(148, 163, 184));
            label.setHorizontalAlignment(SwingConstants.CENTER);

            if (isSelected) {
                label.setBackground(new Color(30, 41, 59));
            } else {
                label.setBackground(row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31));
            }

            return label;
        }
    }
}