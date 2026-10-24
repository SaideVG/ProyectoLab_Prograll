package universidad.sigelab.view;

import universidad.sigelab.controller.ReporteController;
import universidad.sigelab.controller.UsuarioController;
import universidad.sigelab.enums.Modulo;
import universidad.sigelab.enums.RolUsuario;
import universidad.sigelab.model.Dashboard;
import universidad.sigelab.model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MainForm extends JFrame {

    private final Usuario usuario;
    private final UsuarioController usuarioController;
    private final ReporteController reporteController;

    // Métricas KPI
    private JLabel lblTotalEquipos;
    private JLabel lblDisponibles;
    private JLabel lblPrestados;
    private JLabel lblDanados;
    private JLabel lblEnMantenimiento;
    private JLabel lblDeBaja;
    private JLabel lblPrestamosActivos;
    private JLabel lblPrestamosVencidos;

    public MainForm(Usuario usuario) {
        this(usuario, new UsuarioController(), new ReporteController());
    }

    public MainForm(Usuario usuario, UsuarioController usuarioController, ReporteController reporteController) {
        this.usuario = usuario;
        this.usuarioController = usuarioController;
        this.reporteController = reporteController;

        setTitle("SIGELAB - Gestión y Control de Laboratorios");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1180, 850);
        setMinimumSize(new Dimension(1000, 750));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }

            @Override
            public void windowActivated(WindowEvent e) {
                cargarDashboard();
            }
        });

        initComponents();
        cargarDashboard();
    }

    public static void mostrar(Usuario usuario) {
        MainForm mainForm = new MainForm(usuario);
        mainForm.setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(new Color(11, 15, 25));

        mainPanel.add(crearBarraSuperior(), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(10, 25, 15, 25));

        contentPanel.add(crearEncabezadoTitulo());
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(crearSeccionKpi());
        contentPanel.add(Box.createVerticalStrut(20));

        JPanel secOp = crearSeccionOperaciones();
        if (secOp.isVisible()) {
            contentPanel.add(secOp);
            contentPanel.add(Box.createVerticalStrut(20));
        }

        JPanel secAdmin = crearSeccionAdministracion();
        if (secAdmin.isVisible()) {
            contentPanel.add(secAdmin);
            contentPanel.add(Box.createVerticalStrut(25));
        }

        contentPanel.add(crearSeccionBotonesFinales());

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        scrollPane.getVerticalScrollBar().setUI(new ScrollBarOscuroUI());
        scrollPane.getHorizontalScrollBar().setUI(new ScrollBarOscuroUI());

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(crearFooter(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel crearBarraSuperior() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(17, 24, 39));
        bar.setBorder(new EmptyBorder(12, 25, 12, 25));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel lblLogo = new JLabel("SIGELAB");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblLogo.setForeground(Color.WHITE);

        JLabel lblStatus = new JLabel("● Online (SQL Server)");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatus.setForeground(new Color(34, 197, 94));

        left.add(lblLogo);
        left.add(lblStatus);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        right.setOpaque(false);

        String hora = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
        JLabel lblTime = new JLabel("Hora: " + hora);
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTime.setForeground(new Color(156, 163, 175));

        String nombreUsuario = "Usuario: ";
        if (usuario != null && usuario.getUsuario() != null && !usuario.getUsuario().isEmpty()) {
            nombreUsuario += usuario.getUsuario();
        } else if (usuario != null && usuario.getNombreCompleto() != null) {
            nombreUsuario += usuario.getNombreCompleto();
        } else {
            nombreUsuario += "Operador";
        }

        JLabel lblUser = new JLabel(nombreUsuario);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(Color.WHITE);

        String rolText = (usuario != null && usuario.getRol() != null && usuario.getRol().getNombre() != null)
                ? usuario.getRol().getNombre().toString() : "ADMIN";
        JLabel lblRol = new JLabel(rolText);
        lblRol.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRol.setForeground(Color.WHITE);
        lblRol.setOpaque(true);
        lblRol.setBackground(obtenerColorRol(rolText));
        lblRol.setBorder(new EmptyBorder(4, 10, 4, 10));

        right.add(lblTime);
        right.add(lblUser);
        right.add(lblRol);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private Color obtenerColorRol(String rol) {
        switch (rol.toUpperCase()) {
            case "ADMIN":
            case "ADMINISTRADOR":
                return new Color(30, 58, 138);
            case "DOCENTE":
            case "CATEDRATICO":
                return new Color(13, 148, 136);
            case "AUXILIAR":
            case "OPERADOR":
                return new Color(217, 119, 6);
            default:
                return new Color(75, 85, 99);
        }
    }

    private JPanel crearEncabezadoTitulo() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("PANEL CENTRAL DE CONTROL DE LABORATORIOS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 21));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Universidad Mariano Gálvez • Programación II");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(156, 163, 175));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(4));
        panel.add(lblSub);
        return panel;
    }

    private JPanel crearSeccionKpi() {
        CardPanel container = new CardPanel(12, new Color(17, 24, 39));
        container.setLayout(new BorderLayout());
        container.setBorder(new EmptyBorder(12, 16, 12, 16));
        container.setMaximumSize(new Dimension(2000, 160));

        JLabel lblHeader = new JLabel("INDICADORES EN TIEMPO REAL");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHeader.setForeground(new Color(156, 163, 175));
        lblHeader.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel grid = new JPanel(new GridLayout(2, 4, 12, 10));
        grid.setOpaque(false);

        lblTotalEquipos = new JLabel("0");
        lblDisponibles = new JLabel("0");
        lblPrestados = new JLabel("0");
        lblDanados = new JLabel("0");
        lblEnMantenimiento = new JLabel("0");
        lblDeBaja = new JLabel("0");
        lblPrestamosActivos = new JLabel("0");
        lblPrestamosVencidos = new JLabel("0");

        grid.add(crearKpiTile("EQUIPOS", lblTotalEquipos, TipoIlustracion.KPI_EQUIPOS, new Color(59, 130, 246)));
        grid.add(crearKpiTile("DISPONIBLES", lblDisponibles, TipoIlustracion.KPI_DISPONIBLE, new Color(34, 197, 94)));
        grid.add(crearKpiTile("PRESTADOS", lblPrestados, TipoIlustracion.PRESTAMO, new Color(6, 182, 212)));
        grid.add(crearKpiTile("DAÑADOS", lblDanados, TipoIlustracion.KPI_DANADO, new Color(239, 68, 68)));

        grid.add(crearKpiTile("MANTENIMIENTO", lblEnMantenimiento, TipoIlustracion.MANTENIMIENTO, new Color(234, 179, 8)));
        grid.add(crearKpiTile("DE BAJA", lblDeBaja, TipoIlustracion.KPI_DE_BAJA, new Color(107, 114, 128)));
        grid.add(crearKpiTile("P. ACTIVOS", lblPrestamosActivos, TipoIlustracion.KPI_RELOJ, new Color(168, 85, 247)));
        grid.add(crearKpiTile("P. VENCIDOS", lblPrestamosVencidos, TipoIlustracion.KPI_VENCIDO, new Color(225, 29, 72)));

        container.add(lblHeader, BorderLayout.NORTH);
        container.add(grid, BorderLayout.CENTER);
        return container;
    }

    private JPanel crearKpiTile(String titulo, JLabel lblValor, TipoIlustracion tipoIcono, Color accentColor) {
        JPanel tile = new JPanel(new BorderLayout(8, 0));
        tile.setBackground(new Color(24, 32, 47));
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, accentColor),
                new EmptyBorder(6, 12, 6, 12)
        ));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(titulo);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(new Color(156, 163, 175));

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblValor.setForeground(Color.WHITE);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblValor);

        IlustracionPanel iconPanel = new IlustracionPanel(tipoIcono, accentColor);

        tile.add(textPanel, BorderLayout.CENTER);
        tile.add(iconPanel, BorderLayout.EAST);
        return tile;
    }

    private JPanel crearSeccionOperaciones() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel lblGroup = new JLabel("OPERACIONES DIARIAS");
        lblGroup.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblGroup.setForeground(new Color(156, 163, 175));
        lblGroup.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel gridTemp = new JPanel();

        agregarTarjetaSiTienePermiso(gridTemp, "GESTIÓN DE EQUIPOS", "Inventario y estado de hardware", TipoIlustracion.HARDWARE, new Color(59, 130, 246), Modulo.EQUIPOS, () -> abrirVentana("Administración de Equipos", new EquipoView(this.usuario).getPanelPrincipal(), 1050, 740));
        agregarTarjetaSiTienePermiso(gridTemp, "REGISTRO PRÉSTAMOS", "Salida de equipos a estudiantes", TipoIlustracion.PRESTAMO, new Color(6, 182, 212), Modulo.PRESTAMOS, () -> abrirVentana("Registro de Préstamos", new PrestamoView(this.usuario).getPanelPrincipal(), 950, 660));
        agregarTarjetaSiTienePermiso(gridTemp, "DEVOLUCIONES", "Ingreso y recepción de préstamos", TipoIlustracion.DEVOLUCION, new Color(16, 185, 129), Modulo.DEVOLUCIONES, () -> abrirVentana("Devoluciones", new DevolucionView(this.usuario).getPanelPrincipal(), 1150, 720));
        agregarTarjetaSiTienePermiso(gridTemp, "MANTENIMIENTO", "Órdenes preventivas y correctivas", TipoIlustracion.MANTENIMIENTO, new Color(245, 158, 11), Modulo.MANTENIMIENTO, () -> abrirVentana("Mantenimiento de Equipos", new MantenimientoView(this.usuario).getPanelPrincipal(), 1200, 720));

        int cantidad = gridTemp.getComponentCount();
        if (cantidad == 0) {
            panel.setVisible(false);
            return panel;
        }

        JPanel grid;
        if (cantidad >= 3) {
            grid = new JPanel(new GridLayout(0, 2, 14, 12));
        } else {
            grid = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 12));
        }
        grid.setOpaque(false);

        for (Component c : gridTemp.getComponents()) {
            grid.add(c);
        }

        panel.add(lblGroup);
        panel.add(Box.createVerticalStrut(6));
        panel.add(grid);
        return panel;
    }

    private JPanel crearSeccionAdministracion() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel lblGroup = new JLabel("ADMINISTRACIÓN Y ANÁLISIS");
        lblGroup.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblGroup.setForeground(new Color(156, 163, 175));
        lblGroup.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel gridTemp = new JPanel();

        agregarTarjetaSiTienePermiso(gridTemp, "REPORTES", "Estadísticas e historial", TipoIlustracion.REPORTES, new Color(139, 92, 246), Modulo.REPORTES, () -> abrirVentana("Reportes", new ReporteView(this.usuario).getPanelPrincipal(), 1200, 680));
        agregarTarjetaSiTienePermiso(gridTemp, "USUARIOS", "Control de acceso y roles", TipoIlustracion.USUARIOS, new Color(236, 72, 153), Modulo.USUARIOS, () -> abrirVentana("Administración de Usuarios", new UsuarioView(this.usuario).getPanelPrincipal(), 950, 700));
        agregarTarjetaSiTienePermiso(gridTemp, "LABORATORIOS", "Infraestructura física", TipoIlustracion.LABORATORIO, new Color(20, 184, 166), Modulo.LABORATORIOS, () -> abrirVentana("Administración de Laboratorios", new LaboratorioView(this.usuario).getPanelPrincipal(), 900, 640));

        // INSTANCIA ACTUALIZADA CON EL USUARIO PARA TIPOS DE EQUIPO:
        agregarTarjetaSiTienePermiso(gridTemp, "TIPOS DE EQUIPO", "Clasificación de catálogo", TipoIlustracion.CATEGORIA, new Color(99, 102, 241), Modulo.TIPOS_EQUIPO, () -> abrirVentana("Tipos de Equipo", new TipoEquipoView(this.usuario).getPanelPrincipal(), 800, 580));

        agregarTarjetaSiTienePermiso(gridTemp, "AUDITORÍA", "Bitácora de trazabilidad", TipoIlustracion.AUDITORIA, new Color(244, 63, 94), Modulo.AUDITORIA, () -> abrirVentana("Auditoría", new AuditoriaView(this.usuario).getPanelPrincipal(), 1150, 660));

        int cantidad = gridTemp.getComponentCount();
        if (cantidad == 0) {
            panel.setVisible(false);
            return panel;
        }

        JPanel grid;
        if (cantidad >= 3) {
            grid = new JPanel(new GridLayout(0, 3, 14, 12));
        } else {
            grid = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 12));
        }
        grid.setOpaque(false);

        for (Component c : gridTemp.getComponents()) {
            grid.add(c);
        }

        panel.add(lblGroup);
        panel.add(Box.createVerticalStrut(6));
        panel.add(grid);
        return panel;
    }

    private void agregarTarjetaSiTienePermiso(JPanel contenedor, String titulo, String subtexto, TipoIlustracion tipoIcono, Color accentColor, Modulo modulo, Runnable accion) {
        boolean puedeAcceder = false;
        if (usuario != null && usuario.getRol() != null && usuario.getRol().getNombre() != null) {
            RolUsuario rol = usuario.getRol().getNombre();
            puedeAcceder = rol.puedeAcceder(modulo);
        }

        if (puedeAcceder) {
            contenedor.add(crearOpcionCard(titulo, subtexto, tipoIcono, accentColor, accion));
        }
    }

    private JPanel crearOpcionCard(String titulo, String subtexto, TipoIlustracion tipoIcono, Color accentColor, Runnable accion) {
        CardPanel card = new CardPanel(10, new Color(20, 27, 41));
        card.setLayout(new BorderLayout(12, 0));

        Dimension cardSize = new Dimension(360, 75);
        card.setPreferredSize(cardSize);
        card.setMaximumSize(cardSize);
        card.setMinimumSize(cardSize);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                new EmptyBorder(12, 16, 12, 16)
        ));

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(titulo);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel(subtexto);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(156, 163, 175));

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(lblSub);

        IlustracionPanel iconPanel = new IlustracionPanel(tipoIcono, accentColor);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(iconPanel, BorderLayout.EAST);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(28, 38, 58));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(20, 27, 41));
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                accion.run();
            }
        });

        return card;
    }

    private JPanel crearSeccionBotonesFinales() {
        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(2000, 45));

        JButton btnCerrarSesion = new JButton("CERRAR SESIÓN");
        btnCerrarSesion.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.setBackground(new Color(225, 29, 72));
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrarSesion.setPreferredSize(new Dimension(0, 45));
        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        JButton btnSalir = new JButton("SALIR DEL SISTEMA");
        btnSalir.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setBackground(new Color(31, 41, 55));
        btnSalir.setFocusPainted(false);
        btnSalir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalir.setPreferredSize(new Dimension(0, 45));
        btnSalir.addActionListener(e -> salir());

        grid.add(btnCerrarSesion);
        grid.add(btnSalir);
        return grid;
    }

    private JPanel crearFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 30, 8, 30));

        JLabel lblLeft = new JLabel("SIGELAB Enterprise v2.4");
        lblLeft.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblLeft.setForeground(new Color(107, 114, 128));

        JLabel lblRight = new JLabel("Servidor: localhost:1433 (Conectado)");
        lblRight.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRight.setForeground(new Color(107, 114, 128));

        footer.add(lblLeft, BorderLayout.WEST);
        footer.add(lblRight, BorderLayout.EAST);
        return footer;
    }

    private void cargarDashboard() {
        try {
            Dashboard dashboard = reporteController.obtenerDashboard();
            lblTotalEquipos.setText(String.valueOf(dashboard.totalEquipos()));
            lblDisponibles.setText(String.valueOf(dashboard.disponibles()));
            lblPrestados.setText(String.valueOf(dashboard.prestados()));
            lblDanados.setText(String.valueOf(dashboard.danados()));
            lblEnMantenimiento.setText(String.valueOf(dashboard.enMantenimiento()));
            lblDeBaja.setText(String.valueOf(dashboard.deBaja()));
            lblPrestamosActivos.setText(String.valueOf(dashboard.prestamosActivos()));
            lblPrestamosVencidos.setText(String.valueOf(dashboard.prestamosVencidos()));
        } catch (Exception e) {
            lblTotalEquipos.setText("-");
            lblDisponibles.setText("-");
            lblPrestados.setText("-");
            lblDanados.setText("-");
            lblEnMantenimiento.setText("-");
            lblDeBaja.setText("-");
            lblPrestamosActivos.setText("-");
            lblPrestamosVencidos.setText("-");
        }
    }

    private void abrirVentana(String titulo, JPanel panel, int ancho, int alto) {
        JFrame ventana = new JFrame(titulo);
        ventana.setContentPane(panel);
        ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ventana.setSize(ancho, alto);
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }

    private void cerrarSesion() {
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Desea cerrar la sesión?",
                "Cerrar sesión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        usuarioController.cerrarSesion();

        for (Window ventana : Window.getWindows()) {
            ventana.dispose();
        }

        LoginForm.mostrar();
    }

    private void salir() {
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Desea salir del sistema?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (respuesta == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public JPanel getPanelPrincipal() {
        return (JPanel) getContentPane();
    }

    private static class CardPanel extends JPanel {
        private final int cornerRadius;
        private Color bgColor;

        public CardPanel(int radius, Color bg) {
            this.cornerRadius = radius;
            this.bgColor = bg;
            setOpaque(false);
        }

        @Override
        public void setBackground(Color bg) {
            this.bgColor = bg;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

            g2.setColor(new Color(40, 52, 73));
            g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius);

            g2.dispose();
        }
    }

    private enum TipoIlustracion {
        HARDWARE, PRESTAMO, DEVOLUCION, MANTENIMIENTO, REPORTES, USUARIOS, LABORATORIO, CATEGORIA, AUDITORIA,
        KPI_EQUIPOS, KPI_DISPONIBLE, KPI_DANADO, KPI_DE_BAJA, KPI_RELOJ, KPI_VENCIDO
    }

    private static class IlustracionPanel extends JPanel {
        private final TipoIlustracion tipo;
        private final Color color;

        public IlustracionPanel(TipoIlustracion tipo, Color color) {
            this.tipo = tipo;
            this.color = color;
            setPreferredSize(new Dimension(38, 38));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
            g2.fillOval(2, 2, 34, 34);

            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (tipo) {
                case KPI_EQUIPOS:
                    g2.drawRoundRect(10, 8, 18, 22, 3, 3);
                    g2.drawLine(14, 14, 24, 14);
                    g2.drawLine(14, 20, 24, 20);
                    break;
                case KPI_DISPONIBLE:
                    g2.drawLine(11, 19, 16, 24);
                    g2.drawLine(16, 24, 27, 13);
                    break;
                case KPI_DANADO:
                    int[] xPoints = {19, 9, 29};
                    int[] yPoints = {9, 27, 27};
                    g2.drawPolygon(xPoints, yPoints, 3);
                    g2.drawLine(19, 16, 19, 20);
                    g2.drawOval(18, 23, 2, 2);
                    break;
                case KPI_DE_BAJA:
                    g2.drawOval(9, 9, 20, 20);
                    g2.drawLine(14, 14, 24, 24);
                    g2.drawLine(24, 14, 14, 24);
                    break;
                case KPI_RELOJ:
                    g2.drawOval(9, 9, 20, 20);
                    g2.drawLine(19, 13, 19, 19);
                    g2.drawLine(19, 19, 23, 19);
                    break;
                case KPI_VENCIDO:
                    g2.drawOval(9, 9, 20, 20);
                    g2.drawLine(19, 13, 19, 21);
                    g2.drawLine(19, 24, 19, 25);
                    break;
                case HARDWARE:
                    g2.drawRoundRect(9, 11, 20, 13, 2, 2);
                    g2.drawLine(14, 24, 24, 24);
                    break;
                case PRESTAMO:
                    g2.drawArc(9, 9, 20, 20, 45, 270);
                    g2.drawLine(23, 11, 27, 15);
                    break;
                case DEVOLUCION:
                    g2.drawArc(9, 9, 20, 20, 180, 270);
                    g2.drawLine(11, 23, 15, 27);
                    break;
                case MANTENIMIENTO:
                    g2.drawOval(12, 12, 14, 14);
                    g2.drawLine(19, 7, 19, 11);
                    g2.drawLine(19, 27, 19, 31);
                    break;
                case REPORTES:
                    g2.drawLine(11, 27, 11, 20);
                    g2.drawLine(19, 27, 19, 11);
                    g2.drawLine(27, 27, 27, 15);
                    break;
                case USUARIOS:
                    g2.drawOval(13, 8, 12, 12);
                    g2.drawArc(7, 20, 24, 12, 0, 180);
                    break;
                case LABORATORIO:
                    g2.drawLine(9, 29, 29, 29);
                    g2.drawLine(19, 9, 19, 29);
                    g2.drawLine(13, 17, 25, 17);
                    break;
                case CATEGORIA:
                    g2.drawRoundRect(9, 9, 20, 20, 4, 4);
                    g2.drawLine(9, 19, 29, 19);
                    break;
                case AUDITORIA:
                    g2.drawOval(9, 9, 18, 18);
                    g2.drawLine(22, 22, 28, 28);
                    break;
            }

            g2.dispose();
        }
    }

    private static class ScrollBarOscuroUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(30, 41, 59);
            this.trackColor = new Color(11, 15, 25);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return crearBotonInvisible();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return crearBotonInvisible();
        }

        private JButton crearBotonInvisible() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            return btn;
        }
    }
}