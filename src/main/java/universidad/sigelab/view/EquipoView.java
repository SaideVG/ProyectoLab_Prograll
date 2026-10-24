package universidad.sigelab.view;

import universidad.sigelab.controller.EquipoController;
import universidad.sigelab.controller.LaboratorioController;
import universidad.sigelab.controller.QrController;
import universidad.sigelab.controller.TipoEquipoController;
import universidad.sigelab.model.CodigoQr;
import universidad.sigelab.model.Equipo;
import universidad.sigelab.model.Laboratorio;
import universidad.sigelab.model.TipoEquipo;
import universidad.sigelab.model.Usuario;
import universidad.sigelab.util.BusinessException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.Optional;

public class EquipoView {

    private JPanel panelPrincipal;

    private JTextField txtId;
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JComboBox<TipoEquipo> cboTipo;
    private JTextField txtMarca;
    private JTextField txtModelo;
    private JTextField txtSerie;
    private JComboBox<Laboratorio> cboLaboratorio;
    private JTextField txtEstado;
    private JTextField txtObservaciones;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnGenerarQr;

    private JTextField txtBuscar;
    private JButton btnBuscar;

    private JTable tblEquipos;
    private JButton btnSalir;

    private Usuario usuarioActual;
    private final EquipoController equipoController;
    private final TipoEquipoController tipoEquipoController;
    private final LaboratorioController laboratorioController;
    private final QrController qrController;

    private static final Color[] PALETA_COLORES = {
            new Color(59, 130, 246),  // Azul
            new Color(16, 185, 129),  // Esmeralda
            new Color(6, 182, 212),   // Cian
            new Color(245, 158, 11),  // Ámbar
            new Color(139, 92, 246),  // Púrpura
            new Color(236, 72, 153),  // Rosa
            new Color(20, 184, 166),  // Teal
            new Color(99, 102, 241)   // Índigo
    };

    public EquipoView() {
        this(null, new EquipoController(), new TipoEquipoController(), new LaboratorioController(), new QrController());
    }

    public EquipoView(Usuario usuario) {
        this(usuario, new EquipoController(), new TipoEquipoController(), new LaboratorioController(), new QrController());
    }

    public EquipoView(EquipoController equipoController, TipoEquipoController tipoEquipoController,
                      LaboratorioController laboratorioController, QrController qrController) {
        this(null, equipoController, tipoEquipoController, laboratorioController, qrController);
    }

    public EquipoView(Usuario usuario, EquipoController equipoController, TipoEquipoController tipoEquipoController,
                      LaboratorioController laboratorioController, QrController qrController) {
        this.usuarioActual = usuario;
        this.equipoController = equipoController;
        this.tipoEquipoController = tipoEquipoController;
        this.laboratorioController = laboratorioController;
        this.qrController = qrController;

        initComponents();
        configurarFormulario();
        configurarEventos();
        cargarEquipos();
    }

    private void initComponents() {
        panelPrincipal = new JPanel(new BorderLayout(0, 12));
        panelPrincipal.setBackground(new Color(11, 15, 25));
        panelPrincipal.setBorder(new EmptyBorder(16, 20, 16, 20));

        panelPrincipal.add(crearHeader(), BorderLayout.NORTH);

        JPanel centroPanel = new JPanel(new BorderLayout(14, 0));
        centroPanel.setOpaque(false);

        centroPanel.add(crearCardFormulario(), BorderLayout.WEST);
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
                g2.setColor(new Color(59, 130, 246));
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

        JLabel lblTitle = new JLabel("GESTIÓN DE EQUIPOS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Inventario, catálogo y control de hardware");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184));

        textGroup.add(lblTitle);
        textGroup.add(lblSub);

        JLabel lblIcono = new JLabel(new IconoMonitorHardware(new Color(59, 130, 246)));

        leftBox.add(textGroup);
        leftBox.add(lblIcono);

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

    private JPanel crearCardFormulario() {
        CardPanel card = new CardPanel(14, new Color(18, 24, 38));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));
        card.setPreferredSize(new Dimension(350, 0));

        JLabel lblTitle = new JLabel("● DATOS DEL EQUIPO");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(56, 189, 248));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(10));

        JPanel formGrid = new JPanel(new GridLayout(0, 2, 8, 8));
        formGrid.setOpaque(false);

        txtId = crearInput("Auto");
        txtCodigo = crearInput("LAP-001");
        txtNombre = crearInput("Nombre equipo");
        txtMarca = crearInput("Marca");
        txtModelo = crearInput("Modelo");
        txtSerie = crearInput("N° Serie");
        txtEstado = crearInput("DISPONIBLE");
        txtObservaciones = crearInput("Observaciones...");

        cboTipo = new JComboBox<>();
        estilarCombo(cboTipo);

        cboLaboratorio = new JComboBox<>();
        estilarCombo(cboLaboratorio);

        formGrid.add(crearCampoBox("ID", txtId));
        formGrid.add(crearCampoBox("CÓDIGO *", txtCodigo));
        formGrid.add(crearCampoBox("NOMBRE *", txtNombre));
        formGrid.add(crearCampoBox("TIPO *", cboTipo));
        formGrid.add(crearCampoBox("MARCA", txtMarca));
        formGrid.add(crearCampoBox("MODELO", txtModelo));
        formGrid.add(crearCampoBox("N° SERIE", txtSerie));
        formGrid.add(crearCampoBox("LABORATORIO *", cboLaboratorio));
        formGrid.add(crearCampoBox("ESTADO", txtEstado));
        formGrid.add(crearCampoBox("OBSERVACIONES", txtObservaciones));

        card.add(formGrid);
        card.add(Box.createVerticalStrut(14));

        JPanel btnBox = new JPanel(new GridLayout(2, 2, 8, 8));
        btnBox.setOpaque(false);
        btnBox.setMaximumSize(new Dimension(2000, 75));

        btnNuevo = new BotonEstilizado("Nuevo", new Color(30, 41, 59), Color.WHITE, new Color(245, 158, 11, 210));
        btnGuardar = new BotonEstilizado("Guardar", new Color(29, 78, 216), Color.WHITE, new Color(6, 182, 212, 230));
        btnActualizar = new BotonEstilizado("Actualizar", new Color(5, 150, 105), Color.WHITE, new Color(16, 185, 129, 230));
        btnGenerarQr = new BotonEstilizado("Generar QR", new Color(109, 40, 217), Color.WHITE, new Color(168, 85, 247, 230));

        btnBox.add(btnNuevo);
        btnBox.add(btnGuardar);
        btnBox.add(btnActualizar);
        btnBox.add(btnGenerarQr);

        card.add(btnBox);
        return card;
    }

    private JPanel crearCampoBox(String titulo, JComponent comp) {
        JPanel box = new JPanel(new BorderLayout(0, 3));
        box.setOpaque(false);

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(new Color(148, 163, 184));

        box.add(lbl, BorderLayout.NORTH);
        box.add(comp, BorderLayout.CENTER);
        return box;
    }

    private <T> void estilarCombo(JComboBox<T> combo) {
        combo.setBackground(new Color(18, 24, 38));
        combo.setForeground(Color.WHITE);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        combo.setMaximumSize(new Dimension(2000, 30));
    }

    private JPanel crearCardTabla() {
        CardPanel card = new CardPanel(14, new Color(18, 24, 38));
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel barSearch = new JPanel(new BorderLayout(8, 0));
        barSearch.setOpaque(false);

        txtBuscar = crearInput("Buscar por código o nombre...");
        btnBuscar = new BotonEstilizado("🔍 Buscar", new Color(30, 58, 138), Color.WHITE, new Color(59, 130, 246, 200));

        barSearch.add(txtBuscar, BorderLayout.CENTER);
        barSearch.add(btnBuscar, BorderLayout.EAST);

        tblEquipos = new JTable();
        configurarEstiloTabla(tblEquipos);

        JScrollPane scroll = new JScrollPane(tblEquipos);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(new Color(11, 15, 25));
        scroll.getViewport().setBackground(new Color(18, 24, 38));

        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new EsquinaTablaOscura(new Color(11, 15, 25)));
        estilarScroll(scroll.getVerticalScrollBar());

        card.add(barSearch, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel crearFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        btnSalir = new BotonEstilizado("➔ Salir", new Color(18, 24, 38), Color.WHITE, new Color(75, 85, 99, 180));
        btnSalir.setPreferredSize(new Dimension(100, 34));

        footer.add(btnSalir, BorderLayout.EAST);
        return footer;
    }

    private JTextField crearInput(String placeholder) {
        JTextField tf = new JTextField();
        tf.setBackground(new Color(18, 24, 38));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        LineBorder borderNormal = new LineBorder(new Color(46, 59, 82), 1, true);
        LineBorder borderFocus = new LineBorder(new Color(59, 130, 246), 2, true);

        tf.setBorder(BorderFactory.createCompoundBorder(borderNormal, new EmptyBorder(4, 6, 4, 6)));

        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tf.isEditable()) {
                    tf.setBorder(BorderFactory.createCompoundBorder(borderFocus, new EmptyBorder(3, 5, 3, 5)));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(borderNormal, new EmptyBorder(4, 6, 4, 6)));
            }
        });

        return tf;
    }

    private void configurarEstiloTabla(JTable tbl) {
        tbl.setBackground(new Color(18, 24, 38));
        tbl.setForeground(new Color(226, 232, 240));
        tbl.setSelectionBackground(new Color(30, 41, 59));
        tbl.setSelectionForeground(Color.WHITE);
        tbl.setRowHeight(38);
        tbl.setShowGrid(false);

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

        tbl.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
                c.setHorizontalAlignment(SwingConstants.LEFT);
                return c;
            }
        });
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
        txtId.setEditable(false);
        txtEstado.setEditable(false);

        tblEquipos.setModel(new TablaSoloLectura("ID", "Código", "Nombre", "Tipo", "Marca", "Modelo", "Laboratorio", "Estado"));
        tblEquipos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblEquipos.getColumnModel().getColumn(0).setCellRenderer(new BadgeIdDinamicoRenderer());
        tblEquipos.getColumnModel().getColumn(1).setCellRenderer(new ChipCodigoDinamicoRenderer());
        tblEquipos.getColumnModel().getColumn(7).setCellRenderer(new PillEstadoDinamicaRenderer());

        limpiarFormulario();
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnGenerarQr.addActionListener(e -> generarQr());
        btnBuscar.addActionListener(e -> buscar());
        btnSalir.addActionListener(e -> salir());

        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) buscar();
            }
        });

        tblEquipos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarEquipo();
        });
    }

    private void cargarCatalogos() {
        try {
            cboTipo.removeAllItems();
            for (TipoEquipo tipo : tipoEquipoController.listarActivos()) cboTipo.addItem(tipo);
            cboTipo.setSelectedIndex(-1);

            cboLaboratorio.removeAllItems();
            for (Laboratorio laboratorio : laboratorioController.listarActivos()) cboLaboratorio.addItem(laboratorio);
            cboLaboratorio.setSelectedIndex(-1);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardar() {
        try {
            Equipo equipo = new Equipo(txtCodigo.getText(), txtNombre.getText(),
                    (TipoEquipo) cboTipo.getSelectedItem(), txtMarca.getText(), txtModelo.getText(),
                    txtSerie.getText(), (Laboratorio) cboLaboratorio.getSelectedItem(), txtObservaciones.getText());

            if (equipoController.guardar(equipo)) {
                JOptionPane.showMessageDialog(panelPrincipal, "Equipo " + equipo.getCodigoInventario() +
                        " guardado correctamente en estado " + equipo.getEstado() + ".", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                cargarEquipos();
            } else {
                JOptionPane.showMessageDialog(panelPrincipal, "No se pudo guardar el equipo.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarEquipos() {
        try {
            mostrarEnTabla(equipoController.listar());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarEnTabla(List<Equipo> equipos) {
        DefaultTableModel modelo = (DefaultTableModel) tblEquipos.getModel();
        modelo.setRowCount(0);

        for (Equipo equipo : equipos) {
            modelo.addRow(new Object[]{
                    equipo.getIdEquipo(),
                    equipo.getCodigoInventario(),
                    equipo.getNombre(),
                    equipo.getTipo(),
                    equipo.getMarca(),
                    equipo.getModelo(),
                    equipo.getLaboratorio(),
                    equipo.getEstado()
            });
        }
    }

    private void actualizar() {
        if (txtId.getText().isBlank()) {
            JOptionPane.showMessageDialog(panelPrincipal, "Debe seleccionar un equipo.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Equipo equipo = new Equipo(Integer.parseInt(txtId.getText()), txtCodigo.getText(), txtNombre.getText(),
                    (TipoEquipo) cboTipo.getSelectedItem(), txtMarca.getText(), txtModelo.getText(),
                    txtSerie.getText(), (Laboratorio) cboLaboratorio.getSelectedItem(), null,
                    txtObservaciones.getText());

            if (equipoController.actualizar(equipo)) {
                JOptionPane.showMessageDialog(panelPrincipal, "Equipo actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                cargarEquipos();
            } else {
                JOptionPane.showMessageDialog(panelPrincipal, "No se pudo actualizar el equipo.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscar() {
        String codigo = txtBuscar.getText().trim();

        if (codigo.isBlank()) {
            limpiarFormulario();
            cargarEquipos();
            return;
        }

        try {
            Optional<Equipo> resultado = equipoController.buscarPorCodigo(codigo);

            if (resultado.isPresent()) {
                mostrarEnTabla(List.of(resultado.get()));
                seleccionarFilaPorId(resultado.get().getIdEquipo());
            } else {
                JOptionPane.showMessageDialog(panelPrincipal, "No se encontró ningún equipo con ese código.", "Búsqueda", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarEquipo() {
        int fila = tblEquipos.getSelectedRow();
        if (fila == -1) return;

        try {
            int idEquipo = (int) tblEquipos.getValueAt(fila, 0);
            equipoController.buscar(idEquipo).ifPresent(this::mostrarEnFormulario);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarEnFormulario(Equipo equipo) {
        txtId.setText(String.valueOf(equipo.getIdEquipo()));
        txtCodigo.setText(equipo.getCodigoInventario());
        txtNombre.setText(equipo.getNombre());
        txtMarca.setText(equipo.getMarca());
        txtModelo.setText(equipo.getModelo());
        txtSerie.setText(equipo.getNumeroSerie());
        txtEstado.setText(equipo.getEstado().toString());
        txtObservaciones.setText(equipo.getObservaciones());
        seleccionarEnCombo(cboTipo, equipo.getTipo());
        seleccionarEnCombo(cboLaboratorio, equipo.getLaboratorio());

        btnGuardar.setEnabled(false);
        btnActualizar.setEnabled(true);
        btnGenerarQr.setEnabled(true);
    }

    private <T> void seleccionarEnCombo(JComboBox<T> combo, T valor) {
        boolean estaEnLaLista = false;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).equals(valor)) estaEnLaLista = true;
        }
        if (!estaEnLaLista) combo.addItem(valor);
        combo.setSelectedItem(valor);
    }

    private void generarQr() {
        if (txtId.getText().isBlank()) {
            JOptionPane.showMessageDialog(panelPrincipal, "Debe seleccionar un equipo.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            CodigoQr qr = qrController.generarParaEquipo(Integer.parseInt(txtId.getText()));

            String[] opciones = {"Guardar como PNG", "Cerrar"};
            int opcion = JOptionPane.showOptionDialog(panelPrincipal, new JLabel(new ImageIcon(qr.imagen())),
                    "Código QR de " + qr.codigoInventario(), JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, opciones, opciones[0]);

            if (opcion == 0) guardarQr(qr);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panelPrincipal, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarQr(CodigoQr qr) {
        JFileChooser selector = new JFileChooser(qrController.carpetaDeImagenes());
        selector.setDialogTitle("Guardar código QR");
        selector.setFileFilter(new FileNameExtensionFilter("Imagen PNG", "png"));
        selector.setSelectedFile(new File(qrController.carpetaDeImagenes(), qr.codigoInventario() + ".png"));

        if (selector.showSaveDialog(panelPrincipal) != JFileChooser.APPROVE_OPTION) return;

        File guardado = qrController.guardar(qr, selector.getSelectedFile());
        JOptionPane.showMessageDialog(panelPrincipal, "Código QR guardado en:\n" + guardado.getAbsolutePath(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void seleccionarFilaPorId(int idEquipo) {
        for (int fila = 0; fila < tblEquipos.getRowCount(); fila++) {
            if ((int) tblEquipos.getValueAt(fila, 0) == idEquipo) {
                tblEquipos.setRowSelectionInterval(fila, fila);
                tblEquipos.scrollRectToVisible(tblEquipos.getCellRect(fila, 0, true));
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
        txtMarca.setText("");
        txtModelo.setText("");
        txtSerie.setText("");
        txtObservaciones.setText("");
        txtBuscar.setText("");

        txtEstado.setText("");
        tblEquipos.clearSelection();

        cargarCatalogos();

        btnGuardar.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnGenerarQr.setEnabled(false);
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

    // ========================================================
    // COMPONENTES Y RENDERERS DINÁMICOS
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

    private static class IconoMonitorHardware implements Icon {
        private final Color color;

        public IconoMonitorHardware(Color color) {
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

            g2.drawRoundRect(x + 7, y + 9, 18, 12, 2, 2);
            g2.drawLine(x + 12, y + 21, x + 20, y + 21);
            g2.drawLine(x + 16, y + 21, x + 16, y + 24);

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
            setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
            setForeground(fgColor);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorder(new EmptyBorder(6, 10, 6, 10));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(glowColor);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);

            g2.setColor(getModel().isPressed() ? bgColor.darker() : bgColor);
            g2.fillRoundRect(2, 2, w - 5, h - 5, 8, 8);

            g2.setColor(new Color(255, 255, 255, 60));
            g2.drawRoundRect(2, 2, w - 5, h - 5, 8, 8);

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

    private static class BadgeIdDinamicoRenderer extends DefaultTableCellRenderer {
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
                    int h = getHeight() - 10;
                    int x = (getWidth() - w) / 2;
                    int y = (getHeight() - h) / 2;

                    g2.setColor(new Color(colorBase.getRed(), colorBase.getGreen(), colorBase.getBlue(), 30));
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

    private static class ChipCodigoDinamicoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(new Color(56, 189, 248));
            label.setHorizontalAlignment(SwingConstants.LEFT);

            if (isSelected) {
                label.setBackground(new Color(30, 41, 59));
            } else {
                label.setBackground(row % 2 == 0 ? new Color(18, 24, 38) : new Color(14, 19, 31));
            }

            return label;
        }
    }

    private static class PillEstadoDinamicaRenderer extends DefaultTableCellRenderer {
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

                    boolean disponible = st.contains("DISPONIBLE");
                    boolean prestado = st.contains("PRESTADO");
                    boolean mant = st.contains("MANTENIMIENTO");

                    Color bg = disponible ? new Color(6, 78, 59) : (prestado ? new Color(12, 74, 96) : (mant ? new Color(110, 70, 15) : new Color(110, 30, 15)));
                    Color border = disponible ? new Color(52, 211, 153) : (prestado ? new Color(56, 189, 248) : (mant ? new Color(245, 158, 11) : new Color(239, 68, 68)));

                    int w = getWidth() - 12;
                    int h = getHeight() - 10;
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
            if (st.contains("DISPONIBLE")) label.setForeground(new Color(52, 211, 153));
            else if (st.contains("PRESTADO")) label.setForeground(new Color(56, 189, 248));
            else if (st.contains("MANTENIMIENTO")) label.setForeground(new Color(245, 158, 11));
            else label.setForeground(new Color(239, 68, 68));

            panel.add(label, BorderLayout.CENTER);
            return panel;
        }
    }
}