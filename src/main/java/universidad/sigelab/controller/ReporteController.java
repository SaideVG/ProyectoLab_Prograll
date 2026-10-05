package universidad.sigelab.controller;

import universidad.sigelab.enums.EstadoEquipo;
import universidad.sigelab.model.Dashboard;
import universidad.sigelab.model.Equipo;
import universidad.sigelab.model.HistorialEquipo;
import universidad.sigelab.model.Mantenimiento;
import universidad.sigelab.model.PrestamoResumen;
import universidad.sigelab.service.ReporteService;

import java.time.LocalDateTime;
import java.util.List;

public class ReporteController {
    private final ReporteService service;
    public ReporteController() { this(new ReporteService()); }
    public ReporteController(ReporteService service) { this.service = service; }
    public Dashboard obtenerDashboard() { return service.obtenerDashboard(); }
    public List<Equipo> equiposPorEstado(EstadoEquipo estado) { return service.equiposPorEstado(estado); }
    public List<PrestamoResumen> prestamosActivos() { return service.prestamosActivos(); }
    public List<PrestamoResumen> prestamosFinalizados() { return service.prestamosFinalizados(); }
    public List<PrestamoResumen> prestamosVencidos() { return service.prestamosVencidos(); }
    public List<PrestamoResumen> prestamosEntreFechas(LocalDateTime desde, LocalDateTime hasta) { return service.prestamosEntreFechas(desde, hasta); }
    public List<HistorialEquipo> historialDeEquipo(String codigoEquipo) { return service.historialDeEquipo(codigoEquipo); }
    public List<Mantenimiento> historialMantenimiento(String codigoEquipo) { return service.historialMantenimiento(codigoEquipo); }
}
