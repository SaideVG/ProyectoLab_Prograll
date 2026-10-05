package universidad.sigelab.view;

import universidad.sigelab.controller.PrestamoController;
import universidad.sigelab.model.DetallePrestamo;
import universidad.sigelab.model.Equipo;
import universidad.sigelab.model.Prestamo;
import universidad.sigelab.util.Fechas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PrestamoView {

    // Componentes vinculados desde PrestamoView.form
    private JPanel panelPrincipal;

    private JTextField txtSolicitante;
    private JTextField txtIdentificacion;
    private JSpinner spnFechaEsperada;
    private JTextField txtObservaciones;

    private JTextField txtCodigoEquipo;
    private JButton btnAgregar;
    private JButton btnQuitar;
    private JTable tblEquipos;

    private JButton btnNuevo;
    private JButton btnConfirmar;
    private JButton btnSalir;

    // La View solamente conoce al Controller.
    private final PrestamoController prestamoController;

    /*
     * Equipos del préstamo que se está armando (el detalle).
     * Mientras no se confirme, nada se guarda ni cambia de estado en la base.
     */
    private final List<Equipo> equipos = new ArrayList<>();


    public PrestamoView() {
        this(new PrestamoController());
    }

    public PrestamoView(PrestamoController prestamoController) {
        this.prestamoController = prestamoController;

        configurarFormulario();
        configurarEventos();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {
        SelectorFecha.configurar(spnFechaEsperada, LocalDateTime.now().plusDays(1));

        tblEquipos.setModel(new TablaSoloLectura("Código", "Nombre", "Tipo", "Laboratorio", "Estado"));
        tblEquipos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        nuevo();
    }

    private void configurarEventos() {
        btnAgregar.addActionListener(e -> agregarEquipo());

        // Enter en el campo de código equivale a presionar Agregar.
        txtCodigoEquipo.addActionListener(e -> agregarEquipo());

        btnQuitar.addActionListener(e -> quitarEquipo());
        btnNuevo.addActionListener(e -> nuevo());
        btnConfirmar.addActionListener(e -> confirmarPrestamo());
        btnSalir.addActionListener(e -> salir());
    }


    /*
     * ========================================================
     * AGREGAR EQUIPO AL DETALLE
     *
     * El Controller busca el código en la base y valida que el
     * equipo exista, esté DISPONIBLE y no esté repetido.
     * Aquí solo se identifica el equipo: su estado no cambia.
     * ========================================================
     */
    private void agregarEquipo() {
        try {
            Equipo equipo = prestamoController.validarEquipoParaPrestamo(txtCodigoEquipo.getText(), equipos);

            equipos.add(equipo);
            mostrarEquipos();

            txtCodigoEquipo.setText("");
            txtCodigoEquipo.requestFocus();
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void quitarEquipo() {
        int fila = tblEquipos.getSelectedRow();
        if (fila == -1) {
            Dialogos.advertir(panelPrincipal, "Seleccione en la tabla el equipo que desea quitar.");
            return;
        }

        // Las filas de la tabla están en el mismo orden que la lista.
        equipos.remove(fila);
        mostrarEquipos();
    }

    private void mostrarEquipos() {
        DefaultTableModel modelo = (DefaultTableModel) tblEquipos.getModel();
        modelo.setRowCount(0);

        for (Equipo equipo : equipos) {
            modelo.addRow(new Object[]{
                    equipo.getCodigoInventario(),
                    equipo.getNombre(),
                    equipo.getTipo(),
                    equipo.getLaboratorio(),
                    equipo.getEstado()
            });
        }
    }


    /*
     * ========================================================
     * CONFIRMAR PRÉSTAMO
     *
     * Se arma el encabezado y un DetallePrestamo por cada equipo.
     * El Service vuelve a validar todo y lo guarda en una sola
     * transacción: encabezado, detalle, estados y auditoría.
     * ========================================================
     */
    private void confirmarPrestamo() {
        try {
            Prestamo prestamo = new Prestamo(txtSolicitante.getText(), txtIdentificacion.getText(), null,
                    SelectorFecha.leer(spnFechaEsperada, "devolución esperada"), null, txtObservaciones.getText());

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


    /*
     * ========================================================
     * NUEVO
     * ========================================================
     */
    private void nuevo() {
        txtSolicitante.setText("");
        txtIdentificacion.setText("");
        txtObservaciones.setText("");
        txtCodigoEquipo.setText("");
        spnFechaEsperada.setValue(Fechas.aDate(LocalDateTime.now().plusDays(1)));

        equipos.clear();
        mostrarEquipos();

        txtSolicitante.requestFocus();
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
