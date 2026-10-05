package universidad.sigelab.view;

import universidad.sigelab.controller.ReporteController;
import universidad.sigelab.enums.EstadoEquipo;
import universidad.sigelab.model.Equipo;
import universidad.sigelab.model.HistorialEquipo;
import universidad.sigelab.model.Mantenimiento;
import universidad.sigelab.model.Prestamo;
import universidad.sigelab.model.PrestamoResumen;
import universidad.sigelab.util.Fechas;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.List;

public class ReporteView {

    private static final String TODOS = "TODOS";

    /** Reportes disponibles. El texto es el que se muestra en el ComboBox. */
    private enum TipoReporte {
        EQUIPOS_POR_ESTADO("Equipos por estado"),
        PRESTAMOS_ACTIVOS("Préstamos activos"),
        PRESTAMOS_FINALIZADOS("Préstamos finalizados"),
        PRESTAMOS_VENCIDOS("Préstamos vencidos"),
        PRESTAMOS_ENTRE_FECHAS("Préstamos entre fechas"),
        HISTORIAL_DE_EQUIPO("Historial de préstamos de un equipo"),
        HISTORIAL_DE_MANTENIMIENTO("Historial de mantenimiento");

        private final String texto;

        TipoReporte(String texto) {
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    // Componentes vinculados desde ReporteView.form
    private JPanel panelPrincipal;

    private JComboBox<TipoReporte> cboReporte;
    private JComboBox<Object> cboEstado;
    private JTextField txtCodigoEquipo;
    private JSpinner spnDesde;
    private JSpinner spnHasta;
    private JButton btnConsultar;
    private JLabel lblTotal;

    private JTable tblReporte;
    private JButton btnSalir;

    // La View solamente conoce al Controller.
    private final ReporteController reporteController;


    public ReporteView() {
        this(new ReporteController());
    }

    public ReporteView(ReporteController reporteController) {
        this.reporteController = reporteController;

        configurarFormulario();
        configurarEventos();
        consultar();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {
        for (TipoReporte reporte : TipoReporte.values()) cboReporte.addItem(reporte);

        cboEstado.addItem(TODOS);
        for (EstadoEquipo estado : EstadoEquipo.values()) cboEstado.addItem(estado);

        // Por defecto, el rango de fechas cubre el último mes.
        LocalDateTime ahora = LocalDateTime.now();
        SelectorFecha.configurar(spnDesde, ahora.minusMonths(1).withHour(0).withMinute(0));
        SelectorFecha.configurar(spnHasta, ahora.withHour(23).withMinute(59));

        tblReporte.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        habilitarFiltros();
    }

    private void configurarEventos() {
        cboReporte.addActionListener(e -> habilitarFiltros());
        btnConsultar.addActionListener(e -> consultar());

        // Enter en el campo de código equivale a presionar Consultar.
        txtCodigoEquipo.addActionListener(e -> consultar());

        btnSalir.addActionListener(e -> salir());
    }

    /*
     * Cada reporte usa filtros distintos: solo se habilitan los que le corresponden.
     */
    private void habilitarFiltros() {
        TipoReporte reporte = (TipoReporte) cboReporte.getSelectedItem();

        cboEstado.setEnabled(reporte == TipoReporte.EQUIPOS_POR_ESTADO);
        spnDesde.setEnabled(reporte == TipoReporte.PRESTAMOS_ENTRE_FECHAS);
        spnHasta.setEnabled(reporte == TipoReporte.PRESTAMOS_ENTRE_FECHAS);
        txtCodigoEquipo.setEnabled(reporte == TipoReporte.HISTORIAL_DE_EQUIPO
                || reporte == TipoReporte.HISTORIAL_DE_MANTENIMIENTO);
    }


    /*
     * ========================================================
     * CONSULTAR
     * Todos los reportes son de solo lectura.
     * ========================================================
     */
    private void consultar() {
        try {
            TipoReporte reporte = (TipoReporte) cboReporte.getSelectedItem();
            if (reporte == null) return;

            switch (reporte) {
                case EQUIPOS_POR_ESTADO -> mostrarEquipos(reporteController.equiposPorEstado(estadoSeleccionado()));
                case PRESTAMOS_ACTIVOS -> mostrarPrestamos(reporteController.prestamosActivos());
                case PRESTAMOS_FINALIZADOS -> mostrarPrestamos(reporteController.prestamosFinalizados());
                case PRESTAMOS_VENCIDOS -> mostrarPrestamos(reporteController.prestamosVencidos());
                case PRESTAMOS_ENTRE_FECHAS -> mostrarPrestamos(reporteController.prestamosEntreFechas(
                        SelectorFecha.leer(spnDesde, "inicio"), SelectorFecha.leer(spnHasta, "fin")));
                case HISTORIAL_DE_EQUIPO -> mostrarHistorial(reporteController.historialDeEquipo(txtCodigoEquipo.getText()));
                case HISTORIAL_DE_MANTENIMIENTO ->
                        mostrarMantenimientos(reporteController.historialMantenimiento(txtCodigoEquipo.getText()));
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    /** Con "TODOS" el Controller recibe null y no filtra por estado. */
    private EstadoEquipo estadoSeleccionado() {
        return cboEstado.getSelectedItem() instanceof EstadoEquipo estado ? estado : null;
    }


    /*
     * ========================================================
     * RESULTADOS
     * Cada reporte define sus propias columnas en la misma JTable.
     * ========================================================
     */
    private void mostrarEquipos(List<Equipo> equipos) {
        TablaSoloLectura modelo = new TablaSoloLectura("Código", "Nombre", "Tipo", "Marca", "Modelo",
                "N° de serie", "Laboratorio", "Estado");

        for (Equipo equipo : equipos) {
            modelo.addRow(new Object[]{
                    equipo.getCodigoInventario(),
                    equipo.getNombre(),
                    equipo.getTipo(),
                    equipo.getMarca(),
                    equipo.getModelo(),
                    equipo.getNumeroSerie(),
                    equipo.getLaboratorio(),
                    equipo.getEstado()
            });
        }
        mostrar(modelo);
    }

    private void mostrarPrestamos(List<PrestamoResumen> prestamos) {
        TablaSoloLectura modelo = new TablaSoloLectura("N°", "Solicitante", "Identificación", "Fecha préstamo",
                "Devolución esperada", "Registró", "Equipos", "Pendientes", "Estado", "Vencido");
        LocalDateTime ahora = LocalDateTime.now();

        for (PrestamoResumen resumen : prestamos) {
            Prestamo prestamo = resumen.prestamo();
            modelo.addRow(new Object[]{
                    prestamo.getIdPrestamo(),
                    prestamo.getSolicitanteNombre(),
                    prestamo.getSolicitanteIdentificacion(),
                    Fechas.texto(prestamo.getFechaPrestamo()),
                    Fechas.texto(prestamo.getFechaEsperadaDevolucion()),
                    prestamo.getUsuarioRegistra().getUsuario(),
                    resumen.totalEquipos(),
                    resumen.pendientes(),
                    prestamo.getEstado(),
                    resumen.vencido(ahora) ? "Sí" : "No"
            });
        }
        mostrar(modelo);
    }

    private void mostrarHistorial(List<HistorialEquipo> historial) {
        TablaSoloLectura modelo = new TablaSoloLectura("N° de préstamo", "Solicitante", "Identificación",
                "Fecha préstamo", "Devolución esperada", "Fecha devolución", "Condición");

        for (HistorialEquipo fila : historial) {
            boolean pendiente = fila.fechaDevolucion() == null;
            modelo.addRow(new Object[]{
                    fila.idPrestamo(),
                    fila.solicitanteNombre(),
                    fila.solicitanteIdentificacion(),
                    Fechas.texto(fila.fechaPrestamo()),
                    Fechas.texto(fila.fechaEsperadaDevolucion()),
                    pendiente ? "PENDIENTE" : Fechas.texto(fila.fechaDevolucion()),
                    pendiente ? "" : fila.condicion()
            });
        }
        mostrar(modelo);
    }

    private void mostrarMantenimientos(List<Mantenimiento> mantenimientos) {
        TablaSoloLectura modelo = new TablaSoloLectura("N°", "Equipo", "Tipo", "Ingreso", "Responsable",
                "Descripción", "Estado", "Resultado", "Salida", "Observaciones");

        for (Mantenimiento mantenimiento : mantenimientos) {
            modelo.addRow(new Object[]{
                    mantenimiento.getIdMantenimiento(),
                    mantenimiento.getEquipo().getCodigoInventario() + " - " + mantenimiento.getEquipo().getNombre(),
                    mantenimiento.getTipo(),
                    Fechas.texto(mantenimiento.getFechaIngreso()),
                    mantenimiento.getResponsable(),
                    mantenimiento.getDescripcion(),
                    mantenimiento.getEstado(),
                    mantenimiento.getResultado() == null ? "" : mantenimiento.getResultado(),
                    Fechas.texto(mantenimiento.getFechaSalida()),
                    mantenimiento.getObservaciones()
            });
        }
        mostrar(modelo);
    }

    private void mostrar(TablaSoloLectura modelo) {
        tblReporte.setModel(modelo);
        lblTotal.setText(modelo.getRowCount() + " registro(s)");
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
