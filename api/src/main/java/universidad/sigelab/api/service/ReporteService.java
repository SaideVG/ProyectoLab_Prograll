package universidad.sigelab.api.service;

import org.springframework.stereotype.Service;
import universidad.sigelab.api.dto.DashboardDto;
import universidad.sigelab.api.dto.DetallePrestamoDto;
import universidad.sigelab.api.dto.EquipoDetalleDto;
import universidad.sigelab.api.dto.EquipoDto;
import universidad.sigelab.api.dto.MantenimientoDto;
import universidad.sigelab.api.dto.PrestamoDetalleDto;
import universidad.sigelab.api.dto.PrestamoDto;
import universidad.sigelab.api.enums.EstadoEquipo;
import universidad.sigelab.api.repository.EquipoRepository;
import universidad.sigelab.api.repository.MantenimientoRepository;
import universidad.sigelab.api.repository.PrestamoRepository;
import universidad.sigelab.api.util.BusinessException;
import universidad.sigelab.api.util.RecursoNoEncontradoException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Consultas de solo lectura para la app: las mismas del módulo de Reportes
 * y del menú principal del escritorio. La app no modifica datos.
 */
@Service
public class ReporteService {

    private final EquipoRepository equipoRepository;
    private final PrestamoRepository prestamoRepository;
    private final MantenimientoRepository mantenimientoRepository;

    public ReporteService(EquipoRepository equipoRepository, PrestamoRepository prestamoRepository,
                          MantenimientoRepository mantenimientoRepository) {
        this.equipoRepository = equipoRepository;
        this.prestamoRepository = prestamoRepository;
        this.mantenimientoRepository = mantenimientoRepository;
    }

    public DashboardDto obtenerDashboard() {
        Map<EstadoEquipo, Integer> porEstado = equipoRepository.contarPorEstado();
        int total = porEstado.values().stream().mapToInt(Integer::intValue).sum();
        return new DashboardDto(total,
                porEstado.getOrDefault(EstadoEquipo.DISPONIBLE, 0),
                porEstado.getOrDefault(EstadoEquipo.PRESTADO, 0),
                porEstado.getOrDefault(EstadoEquipo.DANADO, 0),
                porEstado.getOrDefault(EstadoEquipo.MANTENIMIENTO, 0),
                porEstado.getOrDefault(EstadoEquipo.BAJA, 0),
                prestamoRepository.contarActivos(),
                prestamoRepository.contarVencidos(LocalDateTime.now()));
    }

    /*
     * ========================================================
     * EQUIPOS
     * ========================================================
     */

    /** estado y texto son opcionales: sin ninguno devuelve todos los equipos. */
    public List<EquipoDto> buscarEquipos(String estado, String texto) {
        return equipoRepository.buscar(estadoValido(estado), limpiar(texto));
    }

    /**
     * Lo que se muestra al escanear el QR de un equipo. El QR contiene el
     * código de inventario (así lo genera QrService del escritorio).
     */
    public EquipoDetalleDto detalleDeEquipo(String codigo) {
        String codigoLimpio = limpiar(codigo);
        if (codigoLimpio == null) throw new BusinessException("Indique el código del equipo.");

        EquipoDto equipo = equipoRepository.buscarPorCodigo(codigoLimpio)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un equipo con el código " + codigoLimpio + "."));

        return new EquipoDetalleDto(equipo, prestamoRepository.historialDeEquipo(equipo.id()),
                mantenimientoRepository.listarPorEquipo(equipo.id()));
    }

    /*
     * ========================================================
     * PRÉSTAMOS
     * ========================================================
     */

    /** filtro: ACTIVOS, VENCIDOS o FINALIZADOS. */
    public List<PrestamoDto> listarPrestamos(String filtro) {
        LocalDateTime ahora = LocalDateTime.now();
        String elegido = filtro == null ? "ACTIVOS" : filtro.trim().toUpperCase();

        return switch (elegido) {
            case "ACTIVOS" -> prestamoRepository.listarPorEstado("ACTIVO", ahora);
            case "FINALIZADOS" -> prestamoRepository.listarPorEstado("FINALIZADO", ahora);
            case "VENCIDOS" -> prestamoRepository.listarVencidos(ahora);
            default -> throw new BusinessException("Filtro de préstamos no válido: use ACTIVOS, VENCIDOS o FINALIZADOS.");
        };
    }

    public PrestamoDetalleDto detalleDePrestamo(int idPrestamo) {
        PrestamoDto prestamo = prestamoRepository.buscar(idPrestamo, LocalDateTime.now())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el préstamo N° " + idPrestamo + "."));
        List<DetallePrestamoDto> equipos = prestamoRepository.listarDetalles(idPrestamo);
        return new PrestamoDetalleDto(prestamo, equipos);
    }

    /*
     * ========================================================
     * MANTENIMIENTO
     * ========================================================
     */

    public List<MantenimientoDto> listarMantenimientos(boolean soloEnProceso) {
        return soloEnProceso ? mantenimientoRepository.listarEnProceso() : mantenimientoRepository.listar();
    }

    /*
     * ========================================================
     * VALIDACIONES
     * ========================================================
     */

    private EstadoEquipo estadoValido(String estado) {
        String limpio = limpiar(estado);
        if (limpio == null) return null;
        try {
            return EstadoEquipo.valueOf(limpio.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Estado de equipo no válido: " + limpio + ".");
        }
    }

    private String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
