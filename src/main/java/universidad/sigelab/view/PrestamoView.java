package universidad.sigelab.view;

import universidad.sigelab.controller.PrestamoController;
import universidad.sigelab.controller.QrController;
import universidad.sigelab.model.DetallePrestamo;
import universidad.sigelab.model.Equipo;
import universidad.sigelab.model.Prestamo;
import universidad.sigelab.model.Usuario;
import universidad.sigelab.util.Fechas;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PrestamoView {

    private JPanel panelPrincipal;

    private JTextField txtSolicitante;
    private JTextField txtIdentificacion;
    private JSpinner spnFechaEsperada;
    private JTextField txtObservaciones;

    private JTextField txtCodigoEquipo;
    private JButton btnAgregar;
    private JButton btnLeerQr;
    private JButton btnLeerQrCamara;
    private JButton btnQuitar;
    private JTable tblEquipos;

    private JButton btnNuevo;
    private JButton btnConfirmar;
    private JButton btnSalir;

    private Usuario usuarioActual;
    private final PrestamoController prestamoController;
    private final QrController qrController;

    private final List<Equipo> equipos = new ArrayList<>();

    public PrestamoView() {
        this(null, new PrestamoController(), new QrController());
    }

    public PrestamoView(Usuario usuario) {
        this(usuario, new PrestamoController(), new QrController());
    }

    public PrestamoView(PrestamoController prestamoController, QrController qrController) {
        this(null, prestamoController, qrController);
    }

    public PrestamoView(Usuario usuario, PrestamoController prestamoController, QrController qrController) {
        this.usuarioActual = usuario;
        this.prestamoController = prestamoController;
        this.qrController = qrController;

        construirInterfazCohesiva();
        configurarFormulario();
        configurarEventos();
    }

    private void construirInterfazCohesiva() {
        panelPrincipal = new JPanel(new BorderLayout(0, 14));
        panelPrincipal.setBackground(new Color(11, 15, 25));
        panelPrincipal.setBorder(new EmptyBorder(16, 20, 16, 20));

        panelPrincipal.add(crearHeaderGlobal(), BorderLayout.NORTH);

        JPanel centroPanel = new JPanel();
        centroPanel.setLayout(new BoxLayout(centroPanel, BoxLayout.Y_AXIS));
        centroPanel.setOpaque(false);

        JPanel cardDatos = crearCardDatos();
        JPanel cardEquipos = crearCardEquipos();

        cardDatos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 165));

        centroPanel.add(cardDatos);
        centroPanel.add(Box.createVerticalStrut(12));
        centroPanel.add(cardEquipos);

        panelPrincipal.add(centroPanel, BorderLayout.CENTER);
        panelPrincipal.add(crearFooterBotonera(), BorderLayout.SOUTH);
    }

    private JPanel crearHeaderGlobal() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleContainer = new JPanel(new BorderLayout(12, 0));
        titleContainer.setOpaque(false);

        JPanel barraIndicadora = new JPanel();
        barraIndicadora.setBackground(new Color(6, 182, 212));
        barraIndicadora.setPreferredSize(new Dimension(4, 38));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);

        Icon iconoCianCircular = new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color colorCian = new Color(6, 182, 212);
                g2.setColor(colorCian);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int diameter = 15;
                int arcX = x + 2;
                int arcY = y + 2;

                g2.drawArc(arcX, arcY, diameter, diameter, 45, 280);

                int arrowX = arcX + diameter - 1;
                int arrowY = arcY + (diameter / 2) - 2;
                g2.drawLine(arrowX, arrowY, arrowX + 4, arrowY - 3);
                g2.drawLine(arrowX, arrowY, arrowX - 1, arrowY - 4);

                g2.dispose();
            }

            @Override
            public int getIconWidth() { return 22; }

            @Override
            public int getIconHeight() { return 20; }
        };

        JLabel lblTitle = new JLabel("REGISTRO DE PRÉSTAMOS", iconoCianCircular, SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Salida e historial de equipos, asignación a estudiantes y prácticas");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184));

        titleBox.add(lblTitle);
        titleBox.add(lblSub);

        titleContainer.add(barraIndicadora, BorderLayout.WEST);
        titleContainer.add(titleBox, BorderLayout.CENTER);

        header.add(titleContainer, BorderLayout.WEST);
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

    private JPanel crearCardDatos() {
        CardEstiloSistema card = new CardEstiloSistema(10, new Color(18, 24, 38));
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblHeader = new JLabel("● DATOS DEL PRÉSTAMO // INFORMACIÓN GENERAL");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHeader.setForeground(new Color(96, 165, 250));

        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 8));
        grid.setOpaque(false);

        if (txtSolicitante == null) txtSolicitante = crearInput("Ingrese nombre del solicitante...");
        if (txtIdentificacion == null) txtIdentificacion = crearInput("Número de documento / carné...");
        if (spnFechaEsperada == null) spnFechaEsperada = new JSpinner();
        if (txtObservaciones == null) txtObservaciones = crearInput("Motivo o detalles de la práctica...");

        grid.add(crearCampo("👤 SOLICITANTE *", txtSolicitante));
        grid.add(crearCampo("🪪 IDENTIFICACIÓN *", txtIdentificacion));
        grid.add(crearCampo("📅 DEVOLUCIÓN ESPERADA *", spnFechaEsperada));
        grid.add(crearCampo("💬 OBSERVACIONES", txtObservaciones));

        card.add(lblHeader, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearCardEquipos() {
        CardEstiloSistema card = new CardEstiloSistema(10, new Color(18, 24, 38));
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblHeader = new JLabel("● EQUIPOS DEL PRÉSTAMO // LECTURA Y ASIGNACIÓN");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHeader.setForeground(new Color(96, 165, 250));

        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(false);

        if (txtCodigoEquipo == null) txtCodigoEquipo = crearInput("Escanee o escriba código ej: CAM-001...");

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnBar.setOpaque(false);

        btnAgregar = crearBotonSolido("+ AGREGAR", new Color(37, 99, 235), Color.WHITE);
        btnLeerQr = crearBotonSolido("🖼 QR IMAGEN", new Color(30, 41, 59), Color.WHITE);
        btnLeerQrCamara = crearBotonSolido("📷 QR CÁMARA", new Color(30, 41, 59), Color.WHITE);
        btnQuitar = crearBotonSolido("🗑 QUITAR", new Color(220, 38, 38), Color.WHITE);

        btnBar.add(btnAgregar);
        btnBar.add(btnLeerQr);
        btnBar.add(btnLeerQrCamara);
        btnBar.add(btnQuitar);

        topBar.add(txtCodigoEquipo, BorderLayout.CENTER);
        topBar.add(btnBar, BorderLayout.EAST);

        JPanel headerBox = new JPanel(new BorderLayout(0, 8));
        headerBox.setOpaque(false);
        headerBox.add(lblHeader, BorderLayout.NORTH);
        headerBox.add(topBar, BorderLayout.SOUTH);

        if (tblEquipos == null) tblEquipos = new JTable();
        JScrollPane scroll = new JScrollPane(tblEquipos);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(new Color(11, 15, 25));
        scroll.getViewport().setBackground(new Color(18, 24, 38));

        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new EsquinaTablaOscura(new Color(11, 15, 25)));
        estilarScroll(scroll.getVerticalScrollBar());

        card.add(headerBox, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearFooterBotonera() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        footer.setOpaque(false);

        btnNuevo = crearBotonSolido("📄 NUEVO", new Color(30, 41, 59), Color.WHITE);
        btnNuevo.setPreferredSize(new Dimension(130, 36));

        btnConfirmar = crearBotonSolido("✓ CONFIRMAR PRÉSTAMO", new Color(37, 99, 235), Color.WHITE);
        btnConfirmar.setPreferredSize(new Dimension(200, 36));

        btnSalir = crearBotonSolido("✖ SALIR", new Color(30, 41, 59), Color.WHITE);
        btnSalir.setPreferredSize(new Dimension(120, 36));

        footer.add(btnNuevo);
        footer.add(btnConfirmar);
        footer.add(btnSalir);
        return footer;
    }

    private JPanel crearCampo(String titulo, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);

        JLabel l = new JLabel(titulo);
        l.setFont(new Font("Segoe UI Emoji", Font.BOLD, 10));
        l.setForeground(new Color(148, 163, 184));

        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JTextField crearInput(String placeholder) {
        JTextField tf = new JTextField();
        estilarCampoTexto(tf);
        return tf;
    }

    private void estilarCampoTexto(JTextField tf) {
        if (tf == null) return;
        tf.setBackground(new Color(15, 23, 42));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        LineBorder borderNormal = new LineBorder(new Color(46, 59, 82), 1, true);
        LineBorder borderFocus = new LineBorder(new Color(59, 130, 246), 2, true);

        tf.setBorder(BorderFactory.createCompoundBorder(borderNormal, new EmptyBorder(5, 8, 5, 8)));

        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(borderFocus, new EmptyBorder(4, 7, 4, 7)));
            }
            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(borderNormal, new EmptyBorder(5, 8, 5, 8)));
            }
        });
    }

    private void estilarSpinnerOscuro(JSpinner spinner) {
        if (spinner == null) return;

        spinner.setUI(new BasicSpinnerUI() {
            @Override
            protected Component createNextButton() {
                return crearBotonFlecha(SwingConstants.NORTH);
            }

            @Override
            protected Component createPreviousButton() {
                return crearBotonFlecha(SwingConstants.SOUTH);
            }

            private JButton crearBotonFlecha(int direccion) {
                JButton btn = new JButton(direccion == SwingConstants.NORTH ? "▲" : "▼");
                btn.setFont(new Font("Segoe UI", Font.BOLD, 8));
                btn.setBackground(new Color(30, 41, 59));
                btn.setForeground(new Color(148, 163, 184));
                btn.setFocusPainted(false);
                btn.setBorder(BorderFactory.createLineBorder(new Color(46, 59, 82)));
                return btn;
            }
        });

        spinner.setBackground(new Color(15, 23, 42));

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField ftf = ((JSpinner.DefaultEditor) editor).getTextField();
            ftf.setBackground(new Color(15, 23, 42));
            ftf.setForeground(Color.WHITE);
            ftf.setCaretColor(Color.WHITE);
            ftf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            ftf.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        }

        spinner.setBorder(new LineBorder(new Color(46, 59, 82), 1, true));
    }

    private JButton crearBotonSolido(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(bg.brighter(), 1, true),
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

    private void estilarScroll(JScrollBar sb) {
        if (sb == null) return;
        sb.setBackground(new Color(18, 24, 38));
        sb.setOpaque(true);
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
        estilarCampoTexto(txtSolicitante);
        estilarCampoTexto(txtIdentificacion);
        estilarCampoTexto(txtObservaciones);
        estilarCampoTexto(txtCodigoEquipo);

        if (spnFechaEsperada != null) {
            SelectorFecha.configurar(spnFechaEsperada, LocalDateTime.now().plusDays(1));
            estilarSpinnerOscuro(spnFechaEsperada);
        }

        if (tblEquipos != null) {
            tblEquipos.setModel(new TablaSoloLectura("Código", "Nombre", "Tipo", "Laboratorio", "Estado"));
            tblEquipos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            configurarEstiloTabla();
        }

        nuevo();
    }

    private void configurarEventos() {
        if (btnAgregar != null) btnAgregar.addActionListener(e -> agregarEquipo());

        if (txtCodigoEquipo != null) {
            txtCodigoEquipo.addActionListener(e -> agregarEquipo());
            txtCodigoEquipo.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        agregarEquipo();
                    }
                }
            });
        }

        if (btnLeerQr != null) btnLeerQr.addActionListener(e -> leerQr());
        if (btnLeerQrCamara != null) btnLeerQrCamara.addActionListener(e -> leerQrConCamara());
        if (btnQuitar != null) btnQuitar.addActionListener(e -> quitarEquipo());
        if (btnNuevo != null) btnNuevo.addActionListener(e -> nuevo());
        if (btnConfirmar != null) btnConfirmar.addActionListener(e -> confirmarPrestamo());
        if (btnSalir != null) btnSalir.addActionListener(e -> salir());
    }

    private void agregarEquipo() {
        try {
            if (txtCodigoEquipo == null || txtCodigoEquipo.getText().trim().isEmpty()) return;
            Equipo equipo = prestamoController.validarEquipoParaPrestamo(txtCodigoEquipo.getText().trim(), equipos);

            equipos.add(equipo);
            mostrarEquipos();

            txtCodigoEquipo.setText("");
            txtCodigoEquipo.requestFocus();
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void leerQr() {
        try {
            JFileChooser selector = new JFileChooser(qrController.carpetaDeImagenes());
            selector.setDialogTitle("Seleccione la imagen del código QR");
            selector.setFileFilter(new FileNameExtensionFilter("Imágenes (PNG, JPG, BMP, GIF)", "png", "jpg", "jpeg", "bmp", "gif"));

            if (selector.showOpenDialog(panelPrincipal) != JFileChooser.APPROVE_OPTION) return;

            agregarCodigoLeido(qrController.leerCodigo(selector.getSelectedFile()));
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void leerQrConCamara() {
        try {
            String codigo = new LectorQrCamaraView(qrController).leer(panelPrincipal);
            if (codigo != null) agregarCodigoLeido(codigo);
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void agregarCodigoLeido(String codigo) {
        if (txtCodigoEquipo != null) {
            txtCodigoEquipo.setText(codigo);
            agregarEquipo();
        }
    }

    private void quitarEquipo() {
        if (tblEquipos == null) return;
        int fila = tblEquipos.getSelectedRow();
        if (fila == -1) {
            Dialogos.advertir(panelPrincipal, "Seleccione en la tabla el equipo que desea quitar.");
            return;
        }

        equipos.remove(fila);
        mostrarEquipos();
    }

    private void mostrarEquipos() {
        if (tblEquipos == null) return;
        DefaultTableModel modelo = (DefaultTableModel) tblEquipos.getModel();
        modelo.setRowCount(0);

        for (Equipo equipo : equipos) {
            modelo.addRow(new Object[]{
                    equipo.getCodigoInventario(),
                    equipo.getNombre(),
                    equipo.getTipo() != null ? equipo.getTipo() : "-",
                    equipo.getLaboratorio() != null ? equipo.getLaboratorio() : "-",
                    "ASIGNADO"
            });
        }
    }

    private void confirmarPrestamo() {
        try {
            String solicitante = txtSolicitante != null ? txtSolicitante.getText().trim() : "";
            String identificacion = txtIdentificacion != null ? txtIdentificacion.getText().trim() : "";
            String observaciones = txtObservaciones != null ? txtObservaciones.getText().trim() : "";

            Prestamo prestamo = new Prestamo(
                    solicitante,
                    identificacion,
                    LocalDateTime.now(),
                    SelectorFecha.leer(spnFechaEsperada, "devolución esperada"),
                    this.usuarioActual,
                    observaciones
            );

            for (Equipo equipo : equipos) {
                prestamo.getDetalles().add(new DetallePrestamo(equipo));
            }

            Prestamo registrado = prestamoController.registrarPrestamo(prestamo);

            Dialogos.informar(panelPrincipal, "Préstamo N° " + registrado.getIdPrestamo() + " registrado con " +
                    registrado.getDetalles().size() + " equipo(s).\nLos equipos quedaron en estado PRESTADO.", "Préstamo");
            nuevo();
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void nuevo() {
        if (txtSolicitante != null) txtSolicitante.setText("");
        if (txtIdentificacion != null) txtIdentificacion.setText("");
        if (txtObservaciones != null) txtObservaciones.setText("");
        if (txtCodigoEquipo != null) txtCodigoEquipo.setText("");

        if (spnFechaEsperada != null) {
            spnFechaEsperada.setValue(Fechas.aDate(LocalDateTime.now().plusDays(1)));
        }

        equipos.clear();
        mostrarEquipos();

        if (txtSolicitante != null) txtSolicitante.requestFocus();
    }

    private void salir() {
        Window win = SwingUtilities.getWindowAncestor(panelPrincipal);
        if (win != null) {
            win.dispose();
        }
    }

    private void configurarEstiloTabla() {
        if (tblEquipos == null) return;

        tblEquipos.setBackground(new Color(18, 24, 38));
        tblEquipos.setForeground(new Color(226, 232, 240));
        tblEquipos.setSelectionBackground(new Color(30, 58, 138));
        tblEquipos.setSelectionForeground(Color.WHITE);
        tblEquipos.setRowHeight(38);
        tblEquipos.setShowGrid(false);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setOpaque(true);
                l.setBackground(new Color(11, 15, 25));
                l.setForeground(new Color(148, 163, 184));
                l.setFont(new Font("Segoe UI", Font.BOLD, 11));
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(46, 59, 82)));
                return l;
            }
        };

        if (tblEquipos.getTableHeader() != null) {
            tblEquipos.getTableHeader().setBackground(new Color(11, 15, 25));
            tblEquipos.getTableHeader().setOpaque(true);
            tblEquipos.getTableHeader().setPreferredSize(new Dimension(0, 32));
            tblEquipos.getTableHeader().setDefaultRenderer(headerRenderer);
        }

        DefaultTableCellRenderer renderCodigo = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setOpaque(true);
                c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                c.setHorizontalAlignment(SwingConstants.CENTER);

                if (isSelected) {
                    c.setBackground(new Color(30, 58, 138));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31));
                    c.setForeground(new Color(52, 211, 153));
                }
                return c;
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
                    c.setBackground(new Color(30, 58, 138));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31));
                    c.setForeground(new Color(226, 232, 240));
                }
                return c;
            }
        };

        DefaultTableCellRenderer renderEstadoChip = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setOpaque(false);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 10));
                label.setForeground(new Color(52, 211, 153));
                label.setText("ASIGNADO");

                JPanel panel = new JPanel(new BorderLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        int w = getWidth() - 28;
                        int h = getHeight() - 16;
                        int x = (getWidth() - w) / 2;
                        int y = (getHeight() - h) / 2;

                        g2.setColor(new Color(6, 78, 59, 180));
                        g2.fillRoundRect(x, y, w, h, 14, 14);

                        g2.setColor(new Color(16, 185, 129, 200));
                        g2.drawRoundRect(x, y, w - 1, h - 1, 14, 14);

                        g2.dispose();
                    }
                };

                panel.setOpaque(true);
                panel.setBackground(isSelected ? new Color(30, 58, 138) : (row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31)));
                panel.add(label, BorderLayout.CENTER);
                return panel;
            }
        };

        for (int i = 0; i < tblEquipos.getColumnCount(); i++) {
            if (i == 0) {
                tblEquipos.getColumnModel().getColumn(i).setCellRenderer(renderCodigo);
            } else if (i == 4) {
                tblEquipos.getColumnModel().getColumn(i).setCellRenderer(renderEstadoChip);
            } else {
                tblEquipos.getColumnModel().getColumn(i).setCellRenderer(renderTexto);
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