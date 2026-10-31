package universidad.sigelab.view;

import universidad.sigelab.controller.TipoEquipoController;
import universidad.sigelab.enums.EstadoRegistro;
import universidad.sigelab.model.TipoEquipo;
import universidad.sigelab.model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
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

public class TipoEquipoView {

    // Componentes vinculados desde TipoEquipoView.form
    private JPanel panelPrincipal;

    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtDescripcion;

    private JCheckBox chkActivo;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnActivarDesactivar;

    private JTextField txtBuscar;
    private JButton btnBuscar;

    private JTable tblTipos;
    private JButton btnSalir;

    // Componentes visuales extras
    private JLabel lblConteoTipos;

    // Usuario logueado
    private Usuario usuarioActual;

    // Controlador
    private final TipoEquipoController tipoEquipoController;

    public TipoEquipoView() {
        this(null, new TipoEquipoController());
    }

    public TipoEquipoView(Usuario usuario) {
        this(usuario, new TipoEquipoController());
    }

    public TipoEquipoView(TipoEquipoController tipoEquipoController) {
        this(null, tipoEquipoController);
    }

    public TipoEquipoView(Usuario usuario, TipoEquipoController tipoEquipoController) {
        this.usuarioActual = usuario;
        this.tipoEquipoController = tipoEquipoController;

        construirInterfazCohesiva();
        configurarFormulario();
        configurarEventos();
        cargarTipos();
    }

    private void construirInterfazCohesiva() {
        panelPrincipal = new JPanel(new BorderLayout(0, 18));
        panelPrincipal.setBackground(new Color(11, 15, 25));
        panelPrincipal.setBorder(new EmptyBorder(22, 26, 22, 26));

        panelPrincipal.setPreferredSize(new Dimension(900, 680));
        panelPrincipal.setMinimumSize(new Dimension(850, 620));

        panelPrincipal.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                Window window = SwingUtilities.getWindowAncestor(panelPrincipal);
                if (window != null) {
                    window.setMinimumSize(new Dimension(850, 620));
                    window.setPreferredSize(new Dimension(900, 680));
                    window.pack();
                    window.setLocationRelativeTo(null);
                }
            }
            @Override public void ancestorRemoved(AncestorEvent event) {}
            @Override public void ancestorMoved(AncestorEvent event) {}
        });

        panelPrincipal.add(crearHeaderGlobal(), BorderLayout.NORTH);

        JPanel centroPanel = new JPanel();
        centroPanel.setLayout(new BoxLayout(centroPanel, BoxLayout.Y_AXIS));
        centroPanel.setOpaque(false);

        JPanel cardFormulario = crearCardFormulario();
        JPanel cardTabla = crearCardTabla();

        centroPanel.add(cardFormulario);
        centroPanel.add(Box.createVerticalStrut(18));
        centroPanel.add(cardTabla);

        panelPrincipal.add(centroPanel, BorderLayout.CENTER);
        panelPrincipal.add(crearFooterGlobal(), BorderLayout.SOUTH);
    }

    private JPanel crearHeaderGlobal() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel leftContainer = new JPanel(new BorderLayout(14, 0));
        leftContainer.setOpaque(false);

        // Barra indicadora azul/púrpura (#6366F1)
        JPanel barraIndicadora = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(99, 102, 241));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
            }
        };
        barraIndicadora.setOpaque(false);
        barraIndicadora.setPreferredSize(new Dimension(4, 42));

        JPanel titleAndLogoBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleAndLogoBox.setOpaque(false);

        JLabel lblTitle = new JLabel("TIPOS DE EQUIPO");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);

        // Ícono personalizado nítido y brillante
        JPanel iconCirclePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int size = Math.min(w, h) - 4;
                int x = (w - size) / 2;
                int y = (h - size) / 2;

                // Círculo base
                g2.setColor(new Color(99, 102, 241, 45));
                g2.fillOval(x, y, size, size);

                // Anillo exterior brillante
                g2.setColor(new Color(129, 140, 248));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawOval(x, y, size, size);

                // Dibujo nítido del ícono de catálogo/carpeta
                g2.setColor(new Color(224, 231, 255));
                g2.setStroke(new BasicStroke(1.5f));

                int boxW = 16;
                int boxH = 12;
                int boxX = (w - boxW) / 2;
                int boxY = (h - boxH) / 2 + 1;

                g2.drawRoundRect(boxX, boxY, boxW, boxH, 4, 4);
                g2.drawLine(boxX + 3, boxY + 4, boxX + boxW - 3, boxY + 4);

                g2.dispose();
            }
        };
        iconCirclePanel.setPreferredSize(new Dimension(34, 34));
        iconCirclePanel.setOpaque(false);

        titleAndLogoBox.add(lblTitle);
        titleAndLogoBox.add(iconCirclePanel);

        JLabel lblSub = new JLabel("Clasificación de catálogo");
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
        CardEstiloSistema card = new CardEstiloSistema(16, new Color(18, 24, 38));
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel topHeaderBox = new JPanel(new BorderLayout());
        topHeaderBox.setOpaque(false);

        JLabel lblTag = new JLabel(" DATOS DEL TIPO DE EQUIPO // REGISTRO Y EDICIÓN ");
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTag.setForeground(new Color(56, 189, 248));
        lblTag.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(14, 116, 144), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));

        JLabel lblEstadoTop = new JLabel("☑ ACTIVO");
        lblEstadoTop.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        lblEstadoTop.setForeground(new Color(52, 211, 153));

        topHeaderBox.add(lblTag, BorderLayout.WEST);
        topHeaderBox.add(lblEstadoTop, BorderLayout.EAST);

        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setOpaque(false);

        JPanel grid = new JPanel(new GridLayout(1, 1, 20, 12));
        grid.setOpaque(false);

        if (txtId == null) txtId = new JTextField();
        if (txtNombre == null) txtNombre = new JTextField();
        if (txtDescripcion == null) txtDescripcion = new JTextField();
        if (chkActivo == null) chkActivo = new JCheckBox("Activo");

        estilarCampoTexto(txtId);
        estilarCampoTexto(txtNombre);
        estilarCampoTexto(txtDescripcion);

        grid.add(crearCampo("🏷️ NOMBRE DEL TIPO *", txtNombre));

        JPanel panelDesc = new JPanel(new BorderLayout());
        panelDesc.setOpaque(false);
        panelDesc.setBorder(new EmptyBorder(10, 0, 0, 0));
        panelDesc.add(crearCampo("📝 DESCRIPCIÓN DETALLADA", txtDescripcion), BorderLayout.CENTER);

        /*
         * El ID lo asigna la base de datos al guardar (IDENTITY), así que el usuario no lo escribe.
         * El campo sigue en el formulario, pero oculto: al seleccionar una fila guarda el ID del tipo
         * que se va a actualizar o desactivar. En la tabla el ID sí se muestra.
         * BoxLayout no le reserva espacio a un componente oculto.
         */
        JPanel campoId = crearCampo("🆔 ID IDENTIFICADOR", txtId);
        campoId.setVisible(false);
        formContainer.add(campoId);

        formContainer.add(grid);
        formContainer.add(panelDesc);

        // Botones redondos con bordes suaves
        JPanel botoneraForm = new JPanel(new GridLayout(1, 4, 14, 0));
        botoneraForm.setOpaque(false);
        botoneraForm.setBorder(new EmptyBorder(12, 0, 0, 0));

        btnNuevo = new BotonRedondeado("+ NUEVO", new Color(15, 23, 42), new Color(56, 189, 248), new Color(30, 58, 138), 12);
        btnGuardar = new BotonRedondeado("🔒 GUARDAR", Color.WHITE, new Color(15, 23, 42), Color.WHITE, 12);
        btnActualizar = new BotonRedondeado("↻ ACTUALIZAR", new Color(15, 23, 42), new Color(148, 163, 184), new Color(51, 65, 85), 12);
        btnActivarDesactivar = new BotonRedondeado("🚫 DESACTIVAR", new Color(15, 23, 42), new Color(248, 113, 113), new Color(153, 27, 27), 12);

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
        CardEstiloSistema card = new CardEstiloSistema(16, new Color(18, 24, 38));
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel searchBar = new JPanel(new BorderLayout(14, 0));
        searchBar.setOpaque(false);

        JLabel lblSearch = new JLabel("🔍 Buscar por nombre");
        lblSearch.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        lblSearch.setForeground(Color.WHITE);

        if (txtBuscar == null) txtBuscar = new JTextField();
        estilarCampoTexto(txtBuscar);

        btnBuscar = new BotonRedondeado("🔍 BUSCAR", new Color(30, 41, 59), Color.WHITE, new Color(51, 65, 85), 10);
        btnBuscar.setPreferredSize(new Dimension(120, 38));

        searchBar.add(lblSearch, BorderLayout.WEST);
        searchBar.add(txtBuscar, BorderLayout.CENTER);
        searchBar.add(btnBuscar, BorderLayout.EAST);

        if (tblTipos == null) tblTipos = new JTable();
        JScrollPane scroll = new JScrollPane(tblTipos);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(18, 24, 38));
        scroll.setPreferredSize(new Dimension(800, 240));

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

        lblConteoTipos = new JLabel("● Mostrando 0 tipos de equipo registrados en el catálogo");
        lblConteoTipos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblConteoTipos.setForeground(new Color(148, 163, 184));

        btnSalir = new BotonRedondeado("✖ SALIR", new Color(30, 41, 59), Color.WHITE, new Color(51, 65, 85), 10);
        btnSalir.setPreferredSize(new Dimension(120, 38));

        footer.add(lblConteoTipos, BorderLayout.WEST);
        footer.add(btnSalir, BorderLayout.EAST);

        return footer;
    }

    private JPanel crearCampo(String titulo, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);

        JLabel l = new JLabel(titulo);
        l.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
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
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 36));

        LineBorder borderNormal = new LineBorder(new Color(46, 59, 82), 1, true);
        LineBorder borderFocus = new LineBorder(new Color(59, 130, 246), 2, true);

        tf.setBorder(BorderFactory.createCompoundBorder(borderNormal, new EmptyBorder(5, 10, 5, 10)));

        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(borderFocus, new EmptyBorder(4, 9, 4, 9)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(borderNormal, new EmptyBorder(5, 10, 5, 10)));
            }
        });
    }

    // CLASE OPTIMIZADA PARA DIBUJAR BOTONES CON BORDES REDONDEADOS
    private static class BotonRedondeado extends JButton {
        private final Color bgColor;
        private final Color borderColor;
        private final int cornerRadius;

        public BotonRedondeado(String texto, Color bg, Color fgText, Color borderColor, int cornerRadius) {
            super(texto);
            this.bgColor = bg;
            this.borderColor = borderColor;
            this.cornerRadius = cornerRadius;
            setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
            setForeground(fgText);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(getPreferredSize().width, 38));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Fondo curvado
            g2.setColor(bgColor);
            g2.fillRoundRect(1, 1, width - 2, height - 2, cornerRadius, cornerRadius);

            // Borde curvado
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(1, 1, width - 3, height - 3, cornerRadius, cornerRadius);

            g2.dispose();
            super.paintComponent(g);
        }
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

        tblTipos.setModel(new TablaSoloLectura("ID", "NOMBRE", "DESCRIPCIÓN", "ACTIVO"));
        tblTipos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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

        tblTipos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarTipo();
        });
    }

    private void guardar() {
        try {
            TipoEquipo tipo = new TipoEquipo(txtNombre.getText(), txtDescripcion.getText());

            if (tipoEquipoController.guardar(tipo)) {
                Dialogos.informar(panelPrincipal, "Tipo de equipo guardado correctamente.", "Tipo de equipo");
                limpiarFormulario();
                cargarTipos();
            } else {
                Dialogos.error(panelPrincipal, "No se pudo guardar el tipo de equipo.");
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void cargarTipos() {
        try {
            mostrarEnTabla(tipoEquipoController.listar());
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void mostrarEnTabla(List<TipoEquipo> tipos) {
        DefaultTableModel modelo = (DefaultTableModel) tblTipos.getModel();
        modelo.setRowCount(0);

        for (TipoEquipo tipo : tipos) {
            modelo.addRow(new Object[]{
                    tipo.getIdTipoEquipo(),
                    tipo.getNombre(),
                    tipo.getDescripcion(),
                    tipo.getEstado() == EstadoRegistro.ACTIVO ? "Sí" : "No"
            });
        }

        if (lblConteoTipos != null) {
            lblConteoTipos.setText("● Mostrando " + tipos.size() + " tipos de equipo registrados en el catálogo");
        }
    }

    private void actualizar() {
        if (txtId.getText().isBlank()) {
            Dialogos.advertir(panelPrincipal, "Debe seleccionar un tipo de equipo.");
            return;
        }

        try {
            TipoEquipo tipo = new TipoEquipo(Integer.parseInt(txtId.getText()), txtNombre.getText(),
                    txtDescripcion.getText(), null);

            if (tipoEquipoController.actualizar(tipo)) {
                Dialogos.informar(panelPrincipal, "Tipo de equipo actualizado correctamente.", "Tipo de equipo");
                limpiarFormulario();
                cargarTipos();
            } else {
                Dialogos.error(panelPrincipal, "No se pudo actualizar el tipo de equipo.");
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void cambiarEstado() {
        if (txtId.getText().isBlank()) {
            Dialogos.advertir(panelPrincipal, "Debe seleccionar un tipo de equipo.");
            return;
        }

        boolean activar = !chkActivo.isSelected();
        String accion = activar ? "activar" : "desactivar";
        if (!Dialogos.confirmar(panelPrincipal, "¿Está seguro de " + accion + " el tipo de equipo?", "Confirmar")) return;

        try {
            int idTipo = Integer.parseInt(txtId.getText());
            EstadoRegistro nuevoEstado = activar ? EstadoRegistro.ACTIVO : EstadoRegistro.INACTIVO;

            if (tipoEquipoController.cambiarEstado(idTipo, nuevoEstado)) {
                Dialogos.informar(panelPrincipal,
                        activar ? "Tipo de equipo activado correctamente." : "Tipo de equipo desactivado correctamente.",
                        "Tipo de equipo");
                cargarTipos();
                seleccionarFilaPorId(idTipo);
            } else {
                Dialogos.error(panelPrincipal, "No se pudo cambiar el estado del tipo de equipo.");
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void buscar() {
        String nombre = txtBuscar.getText().trim();

        if (nombre.isBlank()) {
            limpiarFormulario();
            cargarTipos();
            return;
        }

        try {
            Optional<TipoEquipo> resultado = tipoEquipoController.buscarPorNombre(nombre);

            if (resultado.isPresent()) {
                mostrarEnTabla(List.of(resultado.get()));
                seleccionarFilaPorId(resultado.get().getIdTipoEquipo());
            } else {
                Dialogos.informar(panelPrincipal, "No se encontró ningún tipo de equipo con ese nombre.", "Búsqueda");
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void seleccionarTipo() {
        int fila = tblTipos.getSelectedRow();
        if (fila == -1) return;

        try {
            int idTipo = (int) tblTipos.getValueAt(fila, 0);
            tipoEquipoController.buscar(idTipo).ifPresent(this::mostrarEnFormulario);
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void mostrarEnFormulario(TipoEquipo tipo) {
        boolean activo = tipo.getEstado() == EstadoRegistro.ACTIVO;

        txtId.setText(String.valueOf(tipo.getIdTipoEquipo()));
        txtNombre.setText(tipo.getNombre());
        txtDescripcion.setText(tipo.getDescripcion());
        chkActivo.setSelected(activo);

        btnGuardar.setEnabled(false);
        btnActualizar.setEnabled(true);
        btnActivarDesactivar.setEnabled(true);
        btnActivarDesactivar.setText(activo ? "🚫 DESACTIVAR" : "✓ ACTIVAR");
    }

    private void seleccionarFilaPorId(int idTipo) {
        for (int fila = 0; fila < tblTipos.getRowCount(); fila++) {
            if ((int) tblTipos.getValueAt(fila, 0) == idTipo) {
                tblTipos.setRowSelectionInterval(fila, fila);
                tblTipos.scrollRectToVisible(tblTipos.getCellRect(fila, 0, true));
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
        txtDescripcion.setText("");
        txtBuscar.setText("");

        chkActivo.setSelected(true);
        tblTipos.clearSelection();

        btnGuardar.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnActivarDesactivar.setEnabled(false);
        btnActivarDesactivar.setText("🚫 DESACTIVAR");
    }

    private void salir() {
        Window win = SwingUtilities.getWindowAncestor(panelPrincipal);
        if (win != null) {
            win.dispose();
        }
    }

    private void configurarEstiloTabla() {
        if (tblTipos == null) return;

        tblTipos.setBackground(new Color(18, 24, 38));
        tblTipos.setForeground(new Color(226, 232, 240));
        tblTipos.setSelectionBackground(new Color(30, 58, 138));
        tblTipos.setSelectionForeground(Color.WHITE);
        tblTipos.setRowHeight(40);
        tblTipos.setShowGrid(false);

        if (tblTipos.getTableHeader() != null) {
            tblTipos.getTableHeader().setBackground(new Color(11, 15, 25));
            tblTipos.getTableHeader().setForeground(new Color(148, 163, 184));
            tblTipos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            tblTipos.getTableHeader().setPreferredSize(new Dimension(0, 36));
        }

        DefaultTableCellRenderer renderEstadoChip = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setOpaque(false);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));

                boolean esActivo = value != null && (value.toString().equalsIgnoreCase("Sí") || value.toString().equalsIgnoreCase("ACTIVO"));
                label.setText(esActivo ? "● Sí" : "● No");
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
                panel.setBackground(isSelected ? new Color(30, 58, 138) : (row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31)));
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
                    c.setBackground(new Color(30, 58, 138));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31));
                    c.setForeground(new Color(226, 232, 240));
                }
                return c;
            }
        };

        for (int i = 0; i < tblTipos.getColumnCount(); i++) {
            if (i == 3) {
                tblTipos.getColumnModel().getColumn(i).setCellRenderer(renderEstadoChip);
            } else {
                tblTipos.getColumnModel().getColumn(i).setCellRenderer(renderTexto);
            }
        }
    }

    public JPanel getPanelPrincipal() {
        return panelPrincipal;
    }
}