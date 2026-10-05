package universidad.sigelab.view;

import universidad.sigelab.controller.LaboratorioController;
import universidad.sigelab.enums.EstadoRegistro;
import universidad.sigelab.model.Laboratorio;
import universidad.sigelab.util.BusinessException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Optional;

public class LaboratorioView {

    // Componentes vinculados desde LaboratorioView.form
    private JPanel panelPrincipal;

    private JTextField txtId;
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextField txtUbicacion;
    private JTextField txtCapacidad;

    private JCheckBox chkActivo;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnActivarDesactivar;

    private JTextField txtBuscar;
    private JButton btnBuscar;

    private JTable tblLaboratorios;
    private JButton btnSalir;

    // La View solamente conoce al Controller.
    private final LaboratorioController laboratorioController;


    public LaboratorioView() {
        this(new LaboratorioController());
    }

    public LaboratorioView(LaboratorioController laboratorioController) {
        this.laboratorioController = laboratorioController;

        configurarFormulario();
        configurarEventos();
        cargarLaboratorios();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {
        // El ID lo genera SQL Server.
        txtId.setEditable(false);

        // El estado no se cambia desde el checkbox, sino con el botón Activar/Desactivar.
        chkActivo.setEnabled(false);

        tblLaboratorios.setModel(new TablaSoloLectura("ID", "Código", "Nombre", "Ubicación", "Capacidad", "Activo"));
        tblLaboratorios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        limpiarFormulario();
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnActivarDesactivar.addActionListener(e -> cambiarEstado());
        btnBuscar.addActionListener(e -> buscar());
        btnSalir.addActionListener(e -> salir());

        // Al seleccionar una fila se carga ese laboratorio en el formulario.
        tblLaboratorios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarLaboratorio();
        });
    }


    /*
     * ========================================================
     * GUARDAR
     * ========================================================
     */
    private void guardar() {
        try {
            Laboratorio laboratorio = new Laboratorio(txtCodigo.getText(), txtNombre.getText(),
                    txtUbicacion.getText(), leerCapacidad());

            if (laboratorioController.guardar(laboratorio)) {
                Dialogos.informar(panelPrincipal, "Laboratorio guardado correctamente.", "Laboratorio");
                limpiarFormulario();
                cargarLaboratorios();
            } else {
                Dialogos.error(panelPrincipal, "No se pudo guardar el laboratorio.");
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
    private void cargarLaboratorios() {
        try {
            mostrarEnTabla(laboratorioController.listar());
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void mostrarEnTabla(List<Laboratorio> laboratorios) {
        DefaultTableModel modelo = (DefaultTableModel) tblLaboratorios.getModel();
        modelo.setRowCount(0);

        for (Laboratorio laboratorio : laboratorios) {
            modelo.addRow(new Object[]{
                    laboratorio.getIdLaboratorio(),
                    laboratorio.getCodigo(),
                    laboratorio.getNombre(),
                    laboratorio.getUbicacion(),
                    laboratorio.getCapacidad(),
                    laboratorio.getEstado() == EstadoRegistro.ACTIVO ? "Sí" : "No"
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
            Dialogos.advertir(panelPrincipal, "Debe seleccionar un laboratorio.");
            return;
        }

        try {
            // El estado no viaja desde la pantalla: el Service conserva el que está guardado.
            Laboratorio laboratorio = new Laboratorio(Integer.parseInt(txtId.getText()), txtCodigo.getText(),
                    txtNombre.getText(), txtUbicacion.getText(), leerCapacidad(), null);

            if (laboratorioController.actualizar(laboratorio)) {
                Dialogos.informar(panelPrincipal, "Laboratorio actualizado correctamente.", "Laboratorio");
                limpiarFormulario();
                cargarLaboratorios();
            } else {
                Dialogos.error(panelPrincipal, "No se pudo actualizar el laboratorio.");
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
            Dialogos.advertir(panelPrincipal, "Debe seleccionar un laboratorio.");
            return;
        }

        boolean activar = !chkActivo.isSelected();
        String accion = activar ? "activar" : "desactivar";
        if (!Dialogos.confirmar(panelPrincipal, "¿Está seguro de " + accion + " el laboratorio?", "Confirmar")) return;

        try {
            int idLaboratorio = Integer.parseInt(txtId.getText());
            EstadoRegistro nuevoEstado = activar ? EstadoRegistro.ACTIVO : EstadoRegistro.INACTIVO;

            if (laboratorioController.cambiarEstado(idLaboratorio, nuevoEstado)) {
                Dialogos.informar(panelPrincipal,
                        activar ? "Laboratorio activado correctamente." : "Laboratorio desactivado correctamente.",
                        "Laboratorio");
                cargarLaboratorios();
                seleccionarFilaPorId(idLaboratorio);
            } else {
                Dialogos.error(panelPrincipal, "No se pudo cambiar el estado del laboratorio.");
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }


    /*
     * ========================================================
     * BUSCAR POR CÓDIGO
     * ========================================================
     */
    private void buscar() {
        String codigo = txtBuscar.getText().trim();

        // Sin texto de búsqueda se muestran de nuevo todos los laboratorios.
        if (codigo.isBlank()) {
            limpiarFormulario();
            cargarLaboratorios();
            return;
        }

        try {
            Optional<Laboratorio> resultado = laboratorioController.buscarPorCodigo(codigo);

            if (resultado.isPresent()) {
                mostrarEnTabla(List.of(resultado.get()));
                seleccionarFilaPorId(resultado.get().getIdLaboratorio());
            } else {
                Dialogos.informar(panelPrincipal, "No se encontró ningún laboratorio con ese código.", "Búsqueda");
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
    private void seleccionarLaboratorio() {
        int fila = tblLaboratorios.getSelectedRow();
        if (fila == -1) return;

        try {
            int idLaboratorio = (int) tblLaboratorios.getValueAt(fila, 0);
            laboratorioController.buscar(idLaboratorio).ifPresent(this::mostrarEnFormulario);
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void mostrarEnFormulario(Laboratorio laboratorio) {
        boolean activo = laboratorio.getEstado() == EstadoRegistro.ACTIVO;

        txtId.setText(String.valueOf(laboratorio.getIdLaboratorio()));
        txtCodigo.setText(laboratorio.getCodigo());
        txtNombre.setText(laboratorio.getNombre());
        txtUbicacion.setText(laboratorio.getUbicacion());
        txtCapacidad.setText(String.valueOf(laboratorio.getCapacidad()));
        chkActivo.setSelected(activo);

        // Se está trabajando con un registro existente.
        btnGuardar.setEnabled(false);
        btnActualizar.setEnabled(true);
        btnActivarDesactivar.setEnabled(true);
        btnActivarDesactivar.setText(activo ? "Desactivar" : "Activar");
    }

    private void seleccionarFilaPorId(int idLaboratorio) {
        for (int fila = 0; fila < tblLaboratorios.getRowCount(); fila++) {
            if ((int) tblLaboratorios.getValueAt(fila, 0) == idLaboratorio) {
                tblLaboratorios.setRowSelectionInterval(fila, fila);
                tblLaboratorios.scrollRectToVisible(tblLaboratorios.getCellRect(fila, 0, true));
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
        txtUbicacion.setText("");
        txtCapacidad.setText("");
        txtBuscar.setText("");

        // Todo laboratorio nuevo inicia activo.
        chkActivo.setSelected(true);
        tblLaboratorios.clearSelection();

        btnGuardar.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnActivarDesactivar.setEnabled(false);
        btnActivarDesactivar.setText("Desactivar");
    }

    /** Convertir el texto a número es tarea de la View; que sea mayor que cero lo valida el Service. */
    private int leerCapacidad() {
        try {
            return Integer.parseInt(txtCapacidad.getText().trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("La capacidad debe ser un número entero.");
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
