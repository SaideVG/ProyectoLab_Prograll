package universidad.sigelab.view;

import universidad.sigelab.controller.EquipoController;
import universidad.sigelab.controller.LaboratorioController;
import universidad.sigelab.controller.TipoEquipoController;
import universidad.sigelab.model.Equipo;
import universidad.sigelab.model.Laboratorio;
import universidad.sigelab.model.TipoEquipo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Optional;

public class EquipoView {

    // Componentes vinculados desde EquipoView.form
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

    private JTextField txtBuscar;
    private JButton btnBuscar;

    private JTable tblEquipos;
    private JButton btnSalir;

    // La View solamente conoce a los Controllers.
    private final EquipoController equipoController;
    private final TipoEquipoController tipoEquipoController;
    private final LaboratorioController laboratorioController;


    public EquipoView() {
        this(new EquipoController(), new TipoEquipoController(), new LaboratorioController());
    }

    public EquipoView(EquipoController equipoController, TipoEquipoController tipoEquipoController,
                      LaboratorioController laboratorioController) {
        this.equipoController = equipoController;
        this.tipoEquipoController = tipoEquipoController;
        this.laboratorioController = laboratorioController;

        configurarFormulario();
        configurarEventos();
        cargarEquipos();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {
        // El ID lo genera SQL Server.
        txtId.setEditable(false);

        /*
         * El estado solo se muestra. Cambia como consecuencia de un préstamo,
         * una devolución o un mantenimiento, nunca escribiéndolo aquí.
         */
        txtEstado.setEditable(false);

        tblEquipos.setModel(new TablaSoloLectura("ID", "Código", "Nombre", "Tipo", "Marca", "Modelo", "Laboratorio", "Estado"));
        tblEquipos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        limpiarFormulario();
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnBuscar.addActionListener(e -> buscar());
        btnSalir.addActionListener(e -> salir());

        // Al seleccionar una fila se carga ese equipo en el formulario.
        tblEquipos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarEquipo();
        });
    }

    /*
     * El tipo y el laboratorio se eligen de ComboBox cargados desde sus catálogos
     * (solo registros activos); no se escriben a mano en cada equipo.
     */
    private void cargarCatalogos() {
        try {
            cboTipo.removeAllItems();
            for (TipoEquipo tipo : tipoEquipoController.listarActivos()) cboTipo.addItem(tipo);
            cboTipo.setSelectedIndex(-1);

            cboLaboratorio.removeAllItems();
            for (Laboratorio laboratorio : laboratorioController.listarActivos()) cboLaboratorio.addItem(laboratorio);
            cboLaboratorio.setSelectedIndex(-1);
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }


    /*
     * ========================================================
     * GUARDAR
     * ========================================================
     */
    private void guardar() {
        try {
            Equipo equipo = new Equipo(txtCodigo.getText(), txtNombre.getText(),
                    (TipoEquipo) cboTipo.getSelectedItem(), txtMarca.getText(), txtModelo.getText(),
                    txtSerie.getText(), (Laboratorio) cboLaboratorio.getSelectedItem(), txtObservaciones.getText());

            if (equipoController.guardar(equipo)) {
                Dialogos.informar(panelPrincipal, "Equipo " + equipo.getCodigoInventario() +
                        " guardado correctamente en estado " + equipo.getEstado() + ".", "Equipo");
                limpiarFormulario();
                cargarEquipos();
            } else {
                Dialogos.error(panelPrincipal, "No se pudo guardar el equipo.");
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }


    /*
     * ========================================================
     * LISTAR
     * ========================================================
     */
    private void cargarEquipos() {
        try {
            mostrarEnTabla(equipoController.listar());
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
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


    /*
     * ========================================================
     * ACTUALIZAR
     * ========================================================
     */
    private void actualizar() {
        if (txtId.getText().isBlank()) {
            Dialogos.advertir(panelPrincipal, "Debe seleccionar un equipo.");
            return;
        }

        try {
            // El estado no viaja desde la pantalla: actualizar solo cambia los datos descriptivos.
            Equipo equipo = new Equipo(Integer.parseInt(txtId.getText()), txtCodigo.getText(), txtNombre.getText(),
                    (TipoEquipo) cboTipo.getSelectedItem(), txtMarca.getText(), txtModelo.getText(),
                    txtSerie.getText(), (Laboratorio) cboLaboratorio.getSelectedItem(), null,
                    txtObservaciones.getText());

            if (equipoController.actualizar(equipo)) {
                Dialogos.informar(panelPrincipal, "Equipo actualizado correctamente.", "Equipo");
                limpiarFormulario();
                cargarEquipos();
            } else {
                Dialogos.error(panelPrincipal, "No se pudo actualizar el equipo.");
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }


    /*
     * ========================================================
     * BUSCAR POR CÓDIGO DE INVENTARIO
     * ========================================================
     */
    private void buscar() {
        String codigo = txtBuscar.getText().trim();

        // Sin texto de búsqueda se muestran de nuevo todos los equipos.
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
                Dialogos.informar(panelPrincipal, "No se encontró ningún equipo con ese código.", "Búsqueda");
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }


    /*
     * ========================================================
     * SELECCIÓN DESDE JTable
     * ========================================================
     */
    private void seleccionarEquipo() {
        int fila = tblEquipos.getSelectedRow();
        if (fila == -1) return;

        try {
            int idEquipo = (int) tblEquipos.getValueAt(fila, 0);
            equipoController.buscar(idEquipo).ifPresent(this::mostrarEnFormulario);
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
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

        // Se está trabajando con un registro existente.
        btnGuardar.setEnabled(false);
        btnActualizar.setEnabled(true);
    }

    /*
     * Selecciona en el ComboBox el registro del equipo (equals() compara por ID).
     * Si ya no aparece en la lista porque quedó inactivo, se agrega para poder mostrarlo.
     */
    private <T> void seleccionarEnCombo(JComboBox<T> combo, T valor) {
        boolean estaEnLaLista = false;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).equals(valor)) estaEnLaLista = true;
        }
        if (!estaEnLaLista) combo.addItem(valor);
        combo.setSelectedItem(valor);
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


    /*
     * ========================================================
     * NUEVO
     * ========================================================
     */
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

        // Todo equipo nuevo inicia DISPONIBLE; lo asigna el Service al guardar.
        txtEstado.setText("");
        tblEquipos.clearSelection();

        // Se recargan los catálogos por si cambiaron y para dejar los ComboBox sin selección.
        cargarCatalogos();

        btnGuardar.setEnabled(true);
        btnActualizar.setEnabled(false);
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
