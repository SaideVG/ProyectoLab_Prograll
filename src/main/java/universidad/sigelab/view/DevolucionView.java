package universidad.sigelab.view;

import universidad.sigelab.controller.DevolucionController;
import universidad.sigelab.enums.CondicionDevolucion;
import universidad.sigelab.enums.EstadoPrestamo;
import universidad.sigelab.model.DetallePrestamo;
import universidad.sigelab.model.Devolucion;
import universidad.sigelab.model.Prestamo;
import universidad.sigelab.model.PrestamoResumen;
import universidad.sigelab.util.BusinessException;
import universidad.sigelab.util.Fechas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class DevolucionView {

    // Componentes vinculados desde DevolucionView.form
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

    // La View solamente conoce al Controller.
    private final DevolucionController devolucionController;


    public DevolucionView() {
        this(new DevolucionController());
    }

    public DevolucionView(DevolucionController devolucionController) {
        this.devolucionController = devolucionController;

        configurarFormulario();
        configurarEventos();
        cargarPrestamosActivos();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {
        tblPrestamos.setModel(new TablaSoloLectura("N°", "Solicitante", "Identificación", "Fecha préstamo",
                "Devolución esperada", "Equipos", "Pendientes", "Estado", "Vencido"));
        tblPrestamos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblEquipos.setModel(new TablaSoloLectura("N° detalle", "Código", "Nombre", "Devolución", "Fecha devolución",
                "Observación", "Estado del equipo"));
        tblEquipos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        for (CondicionDevolucion condicion : CondicionDevolucion.values()) cboCondicion.addItem(condicion);
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> buscar());
        btnVerActivos.addActionListener(e -> cargarPrestamosActivos());
        btnDevolver.addActionListener(e -> devolver());
        btnSalir.addActionListener(e -> salir());

        // Al seleccionar un préstamo se muestran sus equipos.
        tblPrestamos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarEquiposDelPrestamo();
        });
    }


    /*
     * ========================================================
     * PRÉSTAMOS
     * ========================================================
     */
    private void cargarPrestamosActivos() {
        try {
            txtBuscar.setText("");
            mostrarPrestamos(devolucionController.listarPrestamosActivos());
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    /*
     * Busca un préstamo por su número aunque ya esté FINALIZADO,
     * para poder revisar cómo se devolvió cada equipo.
     */
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
                Dialogos.informar(panelPrincipal, "No existe el préstamo N° " + idPrestamo + ".", "Búsqueda");
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
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

        // Al cambiar la lista ya no hay préstamo seleccionado.
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


    /*
     * ========================================================
     * EQUIPOS DEL PRÉSTAMO SELECCIONADO
     *
     * Se muestran todos: los pendientes y los ya devueltos.
     * Un equipo sin devolución registrada está PENDIENTE.
     * ========================================================
     */
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
                        detalle.isPendiente() ? "PENDIENTE" : "DEVUELTO " + devolucion.getCondicion(),
                        detalle.isPendiente() ? "" : Fechas.texto(devolucion.getFechaDevolucion()),
                        detalle.isPendiente() ? "" : devolucion.getObservacion(),
                        detalle.getEquipo().getEstado()
                });
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }


    /*
     * ========================================================
     * REGISTRAR DEVOLUCIÓN
     *
     * Se devuelve un equipo a la vez, así un préstamo puede
     * quedar con unos equipos devueltos y otros pendientes.
     * Que el equipo esté pendiente y no se devuelva dos veces
     * lo valida el Service, no esta pantalla.
     * ========================================================
     */
    private void devolver() {
        int filaPrestamo = tblPrestamos.getSelectedRow();
        int filaEquipo = tblEquipos.getSelectedRow();

        if (filaPrestamo == -1 || filaEquipo == -1) {
            Dialogos.advertir(panelPrincipal, "Seleccione un préstamo y el equipo que se devuelve.");
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

            Dialogos.informar(panelPrincipal, "Devolución registrada: " + codigo + " en condición " +
                    devolucion.getCondicion() + ".\n" + (finalizado
                    ? "El préstamo N° " + idPrestamo + " quedó FINALIZADO: ya no tiene equipos pendientes."
                    : "El préstamo N° " + idPrestamo + " sigue ACTIVO: todavía tiene equipos pendientes."), "Devolución");

            txtObservacion.setText("");
            recargar(idPrestamo, finalizado);
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    /*
     * Un préstamo finalizado ya no aparece entre los activos, por eso se
     * muestra solo, para que se vea cómo quedó cada uno de sus equipos.
     */
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
        if (Dialogos.confirmar(panelPrincipal, "¿Desea regresar al menú principal?", "Regresar")) {
            Dialogos.cerrarVentana(panelPrincipal);
        }
    }

    public JPanel getPanelPrincipal() {
        return panelPrincipal;
    }
}
