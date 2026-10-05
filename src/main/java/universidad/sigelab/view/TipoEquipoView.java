package universidad.sigelab.view;

import universidad.sigelab.controller.TipoEquipoController;
import universidad.sigelab.enums.EstadoRegistro;
import universidad.sigelab.model.TipoEquipo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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

    // La View solamente conoce al Controller.
    private final TipoEquipoController tipoEquipoController;


    public TipoEquipoView() {
        this(new TipoEquipoController());
    }

    public TipoEquipoView(TipoEquipoController tipoEquipoController) {
        this.tipoEquipoController = tipoEquipoController;

        configurarFormulario();
        configurarEventos();
        cargarTipos();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {
        // El ID lo genera SQL Server.
        txtId.setEditable(false);

        // El estado no se cambia desde el checkbox, sino con el botón Activar/Desactivar.
        chkActivo.setEnabled(false);

        tblTipos.setModel(new TablaSoloLectura("ID", "Nombre", "Descripción", "Activo"));
        tblTipos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        limpiarFormulario();
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnActivarDesactivar.addActionListener(e -> cambiarEstado());
        btnBuscar.addActionListener(e -> buscar());
        btnSalir.addActionListener(e -> salir());

        // Al seleccionar una fila se carga ese tipo en el formulario.
        tblTipos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarTipo();
        });
    }


    /*
     * ========================================================
     * GUARDAR
     * ========================================================
     */
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


    /*
     * ========================================================
     * LISTAR
     * ========================================================
     */
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
    }


    /*
     * ========================================================
     * ACTUALIZAR
     * ========================================================
     */
    private void actualizar() {
        if (txtId.getText().isBlank()) {
            Dialogos.advertir(panelPrincipal, "Debe seleccionar un tipo de equipo.");
            return;
        }

        try {
            // El estado no viaja desde la pantalla: el Service conserva el que está guardado.
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


    /*
     * ========================================================
     * ACTIVAR / DESACTIVAR
     * El mismo botón hace las dos cosas según el estado actual.
     * ========================================================
     */
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


    /*
     * ========================================================
     * BUSCAR POR NOMBRE
     * ========================================================
     */
    private void buscar() {
        String nombre = txtBuscar.getText().trim();

        // Sin texto de búsqueda se muestran de nuevo todos los tipos.
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


    /*
     * ========================================================
     * SELECCIÓN DESDE JTable
     * ========================================================
     */
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

        // Se está trabajando con un registro existente.
        btnGuardar.setEnabled(false);
        btnActualizar.setEnabled(true);
        btnActivarDesactivar.setEnabled(true);
        btnActivarDesactivar.setText(activo ? "Desactivar" : "Activar");
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


    /*
     * ========================================================
     * NUEVO
     * ========================================================
     */
    private void nuevo() {
        limpiarFormulario();
        txtNombre.requestFocus();
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtBuscar.setText("");

        // Todo tipo nuevo inicia activo.
        chkActivo.setSelected(true);
        tblTipos.clearSelection();

        btnGuardar.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnActivarDesactivar.setEnabled(false);
        btnActivarDesactivar.setText("Desactivar");
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
