package universidad.sigelab.service;

import universidad.sigelab.enums.AccionAuditoria;
import universidad.sigelab.enums.CondicionDevolucion;
import universidad.sigelab.enums.EstadoEquipo;
import universidad.sigelab.enums.EstadoPrestamo;
import universidad.sigelab.enums.Modulo;
import universidad.sigelab.model.DetallePrestamo;
import universidad.sigelab.model.Devolucion;
import universidad.sigelab.model.Equipo;
import universidad.sigelab.model.PrestamoResumen;
import universidad.sigelab.repository.DevolucionRepository;
import universidad.sigelab.repository.EquipoRepository;
import universidad.sigelab.repository.PrestamoRepository;
import universidad.sigelab.util.BusinessException;
import universidad.sigelab.util.Sesion;
import universidad.sigelab.util.Transaccion;
import universidad.sigelab.util.Validador;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class DevolucionService {
    private static final String ENTIDAD = "Devolucion";

    private final DevolucionRepository repository;
    private final PrestamoRepository prestamoRepository;
    private final EquipoRepository equipoRepository;
    private final AuditoriaService auditoriaService;
    public DevolucionService() {
        this(new DevolucionRepository(), new PrestamoRepository(), new EquipoRepository(), new AuditoriaService());
    }
    public DevolucionService(DevolucionRepository repository, PrestamoRepository prestamoRepository,
                             EquipoRepository equipoRepository, AuditoriaService auditoriaService) {
        this.repository = repository;
        this.prestamoRepository = prestamoRepository;
        this.equipoRepository = equipoRepository;
        this.auditoriaService = auditoriaService;
    }

    public List<PrestamoResumen> listarPrestamosActivos() {
        Sesion.exigirAcceso(Modulo.DEVOLUCIONES);
        return prestamoRepository.listarPorEstado(EstadoPrestamo.ACTIVO);
    }

    public Optional<PrestamoResumen> buscarPrestamo(int idPrestamo) {
        Sesion.exigirAcceso(Modulo.DEVOLUCIONES);
        return prestamoRepository.buscarResumen(idPrestamo);
    }

    /** Todos los equipos del préstamo: los pendientes y los que ya fueron devueltos. */
    public List<DetallePrestamo> listarEquipos(int idPrestamo) {
        Sesion.exigirAcceso(Modulo.DEVOLUCIONES);
        return prestamoRepository.listarDetalles(idPrestamo);
    }

    /**
     * Devuelve un equipo de un préstamo. El préstamo no se elimina: se agrega una fila en Devolucion,
     * el equipo cambia de estado y, si ya no quedan pendientes, el préstamo se finaliza.
     */
    public Devolucion registrarDevolucion(int idDetallePrestamo, CondicionDevolucion condicion, String observacion) {
        Sesion.exigirAcceso(Modulo.DEVOLUCIONES);
        if (condicion == null) throw new BusinessException("Seleccione la condición en que regresa el equipo.");
        String observacionLimpia = Validador.opcional(observacion, "Observación", 500);

        DetallePrestamo detalle = prestamoRepository.buscarDetalle(idDetallePrestamo)
                .orElseThrow(() -> new BusinessException("El equipo no pertenece a ningún préstamo."));
        Equipo equipo = detalle.getEquipo();
        int idPrestamo = detalle.getIdPrestamo();

        // RN-11
        if (!detalle.isPendiente()) {
            throw new BusinessException("El equipo " + equipo.getCodigoInventario() +
                    " ya fue devuelto. No puede devolverse dos veces.");
        }
        // RN-10
        PrestamoResumen prestamo = prestamoRepository.buscarResumen(idPrestamo)
                .orElseThrow(() -> new BusinessException("El préstamo ya no existe."));
        if (prestamo.prestamo().getEstado() != EstadoPrestamo.ACTIVO) {
            throw new BusinessException("El préstamo N° " + idPrestamo + " no está activo.");
        }

        // RN-12: BUENO → DISPONIBLE.  RN-13: DAÑADO → DAÑADO.
        EstadoEquipo nuevoEstado = condicion == CondicionDevolucion.BUENO ? EstadoEquipo.DISPONIBLE : EstadoEquipo.DANADO;
        Devolucion devolucion = new Devolucion(idDetallePrestamo, LocalDateTime.now(), condicion,
                observacionLimpia, Sesion.getUsuarioActual());

        return Transaccion.ejecutar("registrar la devolución", connection -> {
            repository.guardar(connection, devolucion);
            boolean cambiado = equipoRepository.cambiarEstado(connection, equipo.getIdEquipo(),
                    EstadoEquipo.PRESTADO, nuevoEstado);
            if (!cambiado) {
                throw new BusinessException("El equipo " + equipo.getCodigoInventario() +
                        " no está PRESTADO. La devolución no se registró.");
            }
            // RN-16: el préstamo sigue ACTIVO mientras quede al menos un equipo pendiente.
            if (prestamoRepository.contarPendientes(connection, idPrestamo) == 0) {
                prestamoRepository.cambiarEstado(connection, idPrestamo, EstadoPrestamo.FINALIZADO);
            }
            auditoriaService.registrar(connection, devolucion.getUsuarioRecibe(), AccionAuditoria.EQUIPO_DEVUELTO,
                    ENTIDAD, devolucion.getIdDevolucion(), "Equipo " + equipo.getCodigoInventario() +
                            " devuelto en condición " + condicion + " (préstamo N° " + idPrestamo + ").");
            return devolucion;
        });
    }
}
