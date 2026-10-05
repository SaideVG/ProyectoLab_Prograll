package universidad.sigelab.view;

import universidad.sigelab.controller.MantenimientoController;
import universidad.sigelab.enums.EstadoEquipo;
import universidad.sigelab.enums.ResultadoMantenimiento;
import universidad.sigelab.enums.TipoMantenimiento;
import universidad.sigelab.model.Equipo;
import universidad.sigelab.model.Mantenimiento;
import universidad.sigelab.util.Fechas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MantenimientoView {

    // Componentes vinculados desde MantenimientoView.form
    private JPanel panelPrincipal;

    // Enviar a mantenimiento
    private JTextField txtCodigoEquipo;
    private JButton btnBuscarEquipo;
    private JLabel lblEquipo;
    private JComboBox<TipoMantenimiento> cboTipo;
    private JTextField txtResponsable;
    private JTextField txtDescripcion;
    private JButton btnEnviar;

    // Historial
    private JCheckBox chkSoloEnProceso;
    private JTable tblMantenimientos;

    // Finalizar mantenimiento
    private JComboBox<ResultadoMantenimiento> cboResultado;
    private JTextField txtObservaciones;
    private JButton btnFinalizar;

    private JButton btnSalir;

    // La View solamente conoce al Controller.
    private final MantenimientoController mantenimientoController;


    public MantenimientoView() {
        this(new MantenimientoController());
    }

    public MantenimientoView(MantenimientoController mantenimientoController) {
        this.mantenimientoController = mantenimientoController;

        configurarFormulario();
        configurarEventos();
        cargarMantenimientos();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {
        for (TipoMantenimiento tipo : TipoMantenimiento.values()) cboTipo.addItem(tipo);
        for (ResultadoMantenimiento resultado : ResultadoMantenimiento.values()) cboResultado.addItem(resultado);

        tblMantenimientos.setModel(new TablaSoloLectura("N°", "Equipo", "Tipo", "Ingreso", "Responsable",
                "Descripción", "Estado", "Resultado", "Salida", "Estado del equipo"));
        tblMantenimientos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        limpiarEnvio();
    }

    private void configurarEventos() {
        btnBuscarEquipo.addActionListener(e -> buscarEquipo());

        // Enter en el campo de código equivale a presionar Buscar.
        txtCodigoEquipo.addActionListener(e -> buscarEquipo());

        btnEnviar.addActionListener(e -> enviarAMantenimiento());
        chkSoloEnProceso.addActionListener(e -> cargarMantenimientos());
        btnFinalizar.addActionListener(e -> finalizarMantenimiento());
        btnSalir.addActionListener(e -> salir());
    }


    /*
     * ========================================================
     * ENVIAR A MANTENIMIENTO
     * ========================================================
     */
    private void buscarEquipo() {
        try {
            Equipo equipo = mantenimientoController.buscarEquipo(txtCodigoEquipo.getText());

            lblEquipo.setText(equipo.getCodigoInventario() + " - " + equipo.getNombre() +
                    "   |   Estado: " + equipo.getEstado());

            // Solo es una sugerencia: un equipo dañado suele necesitar mantenimiento correctivo.
            cboTipo.setSelectedItem(equipo.getEstado() == EstadoEquipo.DANADO
                    ? TipoMantenimiento.CORRECTIVO
                    : TipoMantenimiento.PREVENTIVO);
        } catch (Exception e) {
            lblEquipo.setText("Sin equipo identificado");
            Dialogos.error(panelPrincipal, e);
        }
    }

    /*
     * Qué estados pueden entrar a mantenimiento lo decide el Service.
     * Si acepta, el equipo pasa a MANTENIMIENTO en la misma transacción.
     */
    private void enviarAMantenimiento() {
        try {
            Mantenimiento mantenimiento = mantenimientoController.enviarAMantenimiento(txtCodigoEquipo.getText(),
                    (TipoMantenimiento) cboTipo.getSelectedItem(), txtDescripcion.getText(), txtResponsable.getText());

            Dialogos.informar(panelPrincipal, "Mantenimiento N° " + mantenimiento.getIdMantenimiento() +
                    " registrado.\nEl equipo " + mantenimiento.getEquipo().getCodigoInventario() +
                    " quedó en estado " + mantenimiento.getEquipo().getEstado() + ".", "Mantenimiento");

            limpiarEnvio();
            cargarMantenimientos();
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void limpiarEnvio() {
        txtCodigoEquipo.setText("");
        txtDescripcion.setText("");
        txtResponsable.setText("");
        lblEquipo.setText("Sin equipo identificado");
        cboTipo.setSelectedIndex(0);
    }


    /*
     * ========================================================
     * HISTORIAL
     * ========================================================
     */
    private void cargarMantenimientos() {
        try {
            DefaultTableModel modelo = (DefaultTableModel) tblMantenimientos.getModel();
            modelo.setRowCount(0);

            for (Mantenimiento mantenimiento : mantenimientoController.listar(chkSoloEnProceso.isSelected())) {
                Equipo equipo = mantenimiento.getEquipo();
                modelo.addRow(new Object[]{
                        mantenimiento.getIdMantenimiento(),
                        equipo.getCodigoInventario() + " - " + equipo.getNombre(),
                        mantenimiento.getTipo(),
                        Fechas.texto(mantenimiento.getFechaIngreso()),
                        mantenimiento.getResponsable(),
                        mantenimiento.getDescripcion(),
                        mantenimiento.getEstado(),
                        mantenimiento.getResultado() == null ? "" : mantenimiento.getResultado(),
                        Fechas.texto(mantenimiento.getFechaSalida()),
                        equipo.getEstado()
                });
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }


    /*
     * ========================================================
     * FINALIZAR MANTENIMIENTO
     *
     * REPARADO      → el equipo vuelve a DISPONIBLE
     * NO_REPARABLE  → el equipo pasa a BAJA
     * La regla está en el Service; aquí solo se elige el resultado.
     * ========================================================
     */
    private void finalizarMantenimiento() {
        int fila = tblMantenimientos.getSelectedRow();
        if (fila == -1) {
            Dialogos.advertir(panelPrincipal, "Seleccione en la tabla el mantenimiento que desea finalizar.");
            return;
        }

        try {
            int idMantenimiento = (int) tblMantenimientos.getValueAt(fila, 0);

            Mantenimiento mantenimiento = mantenimientoController.finalizar(idMantenimiento,
                    (ResultadoMantenimiento) cboResultado.getSelectedItem(), txtObservaciones.getText());

            Dialogos.informar(panelPrincipal, "Mantenimiento N° " + idMantenimiento + " finalizado como " +
                    mantenimiento.getResultado() + ".\nEl equipo " + mantenimiento.getEquipo().getCodigoInventario() +
                    " quedó en estado " + mantenimiento.getEquipo().getEstado() + ".", "Mantenimiento");

            txtObservaciones.setText("");
            cargarMantenimientos();
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void salir() {
        if (Dialogos.confirmar(panelPrincipal, "¿Desea regresar al menú principal?", "Regresar")) {
            Dialogos.cerrarVentana(panelPrincipal);
        }
    }

    public JPanel getPanelPrincipal() {
        return panelPrincipal;
    }
}
