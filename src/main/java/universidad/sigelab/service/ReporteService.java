package universidad.sigelab.service;

import universidad.sigelab.enums.EstadoEquipo;
import universidad.sigelab.enums.EstadoPrestamo;
import universidad.sigelab.enums.Modulo;
import universidad.sigelab.model.Dashboard;
import universidad.sigelab.model.Equipo;
import universidad.sigelab.model.HistorialEquipo;
import universidad.sigelab.model.Mantenimiento;
import universidad.sigelab.model.PrestamoResumen;
import universidad.sigelab.repository.EquipoRepository;
import universidad.sigelab.repository.MantenimientoRepository;
import universidad.sigelab.repository.PrestamoRepository;
import universidad.sigelab.util.BusinessException;
import universidad.sigelab.util.Sesion;
import universidad.sigelab.util.Validador;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Consultas de solo lectura: dashboard del menú principal y reportes. */
public class ReporteService {
    private final EquipoRepository equipoRepository;
    private final PrestamoRepository prestamoRepository;
    private final MantenimientoRepository mantenimientoRepository;
    public ReporteService() { this(new EquipoRepository(), new PrestamoRepository(), new MantenimientoRepository()); }
    public ReporteService(EquipoRepository equipoRepository, PrestamoRepository prestamoRepository,
                          MantenimientoRepository mantenimientoRepository) {
        this.equipoRepository = equipoRepository;
        this.prestamoRepository = prestamoRepository;
        this.mantenimientoRepository = mantenimientoRepository;
    }

    public Dashboard obtenerDashboard() {
        Sesion.exigirAcceso(Modulo.REPORTES);
        Map<EstadoEquipo, Integer> porEstado = equipoRepository.contarPorEstado();
        int total = porEstado.values().stream().mapToInt(Integer::intValue).sum();
        return new Dashboard(total,
                porEstado.getOrDefault(EstadoEquipo.DISPONIBLE, 0),
                porEstado.getOrDefault(EstadoEquipo.PRESTADO, 0),
                porEstado.getOrDefault(EstadoEquipo.DANADO, 0),
                porEstado.getOrDefault(EstadoEquipo.MANTENIMIENTO, 0),
                porEstado.getOrDefault(EstadoEquipo.BAJA, 0),
                prestamoRepository.contarActivos(),
                prestamoRepository.contarVencidos(LocalDateTime.now()));
    }

    /** Con estado en null devuelve todos los equipos. */
    public List<Equipo> equiposPorEstado(EstadoEquipo estado) {
        Sesion.exigirAcceso(Modulo.REPORTES);
        return estado == null ? equipoRepository.listar() : equipoRepository.listarPorEstado(estado);
    }

    public List<PrestamoResumen> prestamosActivos() {
        Sesion.exigirAcceso(Modulo.REPORTES);
        return prestamoRepository.listarPorEstado(EstadoPrestamo.ACTIVO);
    }

    public List<PrestamoResumen> prestamosFinalizados() {
        Sesion.exigirAcceso(Modulo.REPORTES);
        return prestamoRepository.listarPorEstado(EstadoPrestamo.FINALIZADO);
    }

    public List<PrestamoResumen> prestamosVencidos() {
        Sesion.exigirAcceso(Modulo.REPORTES);
        return prestamoRepository.listarVencidos(LocalDateTime.now());
    }

    public List<PrestamoResumen> prestamosEntreFechas(LocalDateTime desde, LocalDateTime hasta) {
        Sesion.exigirAcceso(Modulo.REPORTES);
        if (desde == null || hasta == null) throw new BusinessException("Indique las dos fechas del rango.");
        if (desde.isAfter(hasta)) throw new BusinessException("La fecha inicial no puede ser posterior a la fecha final.");
        return prestamoRepository.listarEntreFechas(desde, hasta);
    }

    public List<HistorialEquipo> historialDeEquipo(String codigoEquipo) {
        Sesion.exigirAcceso(Modulo.REPORTES);
        return prestamoRepository.historialDeEquipo(buscarEquipo(codigoEquipo).getIdEquipo());
    }

    /** Con el código vacío devuelve el historial de mantenimiento de todos los equipos. */
    public List<Mantenimiento> historialMantenimiento(String codigoEquipo) {
        Sesion.exigirAcceso(Modulo.REPORTES);
        if (codigoEquipo == null || codigoEquipo.isBlank()) return mantenimientoRepository.listar();
        return mantenimientoRepository.listarPorEquipo(buscarEquipo(codigoEquipo).getIdEquipo());
    }

    private Equipo buscarEquipo(String codigo) {
        String codigoLimpio = Validador.requerido(codigo, "Código de equipo", 30);
        return equipoRepository.buscarPorCodigo(codigoLimpio)
                .orElseThrow(() -> new BusinessException("No existe un equipo con el código " + codigoLimpio + "."));
    }
}
