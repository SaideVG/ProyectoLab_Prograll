package universidad.sigelab.service;

import universidad.sigelab.enums.AccionAuditoria;
import universidad.sigelab.enums.EstadoEquipo;
import universidad.sigelab.enums.EstadoPrestamo;
import universidad.sigelab.enums.Modulo;
import universidad.sigelab.model.DetallePrestamo;
import universidad.sigelab.model.Equipo;
import universidad.sigelab.model.Prestamo;
import universidad.sigelab.repository.EquipoRepository;
import universidad.sigelab.repository.PrestamoRepository;
import universidad.sigelab.util.BusinessException;
import universidad.sigelab.util.Sesion;
import universidad.sigelab.util.Transaccion;
import universidad.sigelab.util.Validador;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PrestamoService {
    private static final String ENTIDAD = "Prestamo";

    private final PrestamoRepository repository;
    private final EquipoRepository equipoRepository;
    private final AuditoriaService auditoriaService;
    public PrestamoService() { this(new PrestamoRepository(), new EquipoRepository(), new AuditoriaService()); }
    public PrestamoService(PrestamoRepository repository, EquipoRepository equipoRepository,
                           AuditoriaService auditoriaService) {
        this.repository = repository;
        this.equipoRepository = equipoRepository;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Identifica un equipo por su código y verifica que pueda agregarse al préstamo que se está armando.
     * Solo consulta: no modifica ningún estado. El código puede venir del teclado o de un QR (RN-19).
     */
    public Equipo validarEquipoParaPrestamo(String codigo, List<Equipo> yaAgregados) {
        Sesion.exigirAcceso(Modulo.PRESTAMOS);
        String codigoLimpio = Validador.requerido(codigo, "Código de equipo", 30);
        Equipo equipo = equipoRepository.buscarPorCodigo(codigoLimpio)
                .orElseThrow(() -> new BusinessException("No existe un equipo con el código " + codigoLimpio + "."));
        // RN-06
        boolean repetido = yaAgregados.stream().anyMatch(agregado -> agregado.getIdEquipo() == equipo.getIdEquipo());
        if (repetido) throw new BusinessException("El equipo " + equipo.getCodigoInventario() + " ya está en este préstamo.");
        exigirDisponible(equipo);
        return equipo;
    }

    /**
     * Confirma el préstamo como una sola operación: encabezado, detalle, cambio de estado
     * de cada equipo y auditoría. Si un paso falla, la transacción deshace los anteriores.
     */
    public Prestamo registrarPrestamo(Prestamo prestamo) {
        Sesion.exigirAcceso(Modulo.PRESTAMOS);
        validar(prestamo);
        prestamo.setFechaPrestamo(LocalDateTime.now());
        prestamo.setUsuarioRegistra(Sesion.getUsuarioActual());
        prestamo.setEstado(EstadoPrestamo.ACTIVO);

        return Transaccion.ejecutar("registrar el préstamo", connection -> {
            repository.guardar(connection, prestamo);
            for (DetallePrestamo detalle : prestamo.getDetalles()) {
                Equipo equipo = detalle.getEquipo();
                // RN-07 y RN-09: el equipo pasa a PRESTADO solo si en este instante sigue DISPONIBLE.
                boolean prestado = equipoRepository.cambiarEstado(connection, equipo.getIdEquipo(),
                        EstadoEquipo.DISPONIBLE, EstadoEquipo.PRESTADO);
                if (!prestado) {
                    throw new BusinessException("El equipo " + equipo.getCodigoInventario() +
                            " ya no está DISPONIBLE. El préstamo no se registró.");
                }
                repository.guardarDetalle(connection, prestamo.getIdPrestamo(), detalle);
                equipo.setEstado(EstadoEquipo.PRESTADO);
            }
            auditoriaService.registrar(connection, prestamo.getUsuarioRegistra(), AccionAuditoria.PRESTAMO_CREADO,
                    ENTIDAD, prestamo.getIdPrestamo(), "Préstamo a " + prestamo.getSolicitanteNombre() +
                            " con " + prestamo.getDetalles().size() + " equipo(s): " + codigos(prestamo));
            return prestamo;
        });
    }

    private void validar(Prestamo prestamo) {
        if (prestamo == null) throw new BusinessException("El préstamo es obligatorio.");
        prestamo.setSolicitanteNombre(Validador.requerido(prestamo.getSolicitanteNombre(), "Solicitante", 150));
        prestamo.setSolicitanteIdentificacion(
                Validador.requerido(prestamo.getSolicitanteIdentificacion(), "Identificación del solicitante", 30));
        prestamo.setObservaciones(Validador.opcional(prestamo.getObservaciones(), "Observaciones", 500));

        LocalDateTime fechaEsperada = prestamo.getFechaEsperadaDevolucion();
        if (fechaEsperada == null) throw new BusinessException("Indique la fecha esperada de devolución.");
        if (!fechaEsperada.isAfter(LocalDateTime.now())) {
            throw new BusinessException("La fecha esperada de devolución debe ser posterior a la fecha y hora actual.");
        }

        // RN-05
        if (prestamo.getDetalles() == null || prestamo.getDetalles().isEmpty()) {
            throw new BusinessException("El préstamo debe tener al menos un equipo.");
        }

        Set<Integer> idsAgregados = new HashSet<>();
        for (DetallePrestamo detalle : prestamo.getDetalles()) {
            if (detalle.getEquipo() == null) throw new BusinessException("El préstamo tiene un equipo sin identificar.");
            // Se usa el estado real de la base, no el que tenía el equipo cuando se agregó en pantalla.
            Equipo equipo = equipoRepository.buscarPorId(detalle.getEquipo().getIdEquipo())
                    .orElseThrow(() -> new BusinessException("Uno de los equipos del préstamo ya no existe."));
            // RN-06
            if (!idsAgregados.add(equipo.getIdEquipo())) {
                throw new BusinessException("El equipo " + equipo.getCodigoInventario() + " está repetido en el préstamo.");
            }
            exigirDisponible(equipo);
            detalle.setEquipo(equipo);
        }
    }

    /** RN-07, RN-08 y RN-14: PRESTADO, DAÑADO, MANTENIMIENTO y BAJA bloquean el préstamo. */
    private void exigirDisponible(Equipo equipo) {
        if (equipo.getEstado() != EstadoEquipo.DISPONIBLE) {
            throw new BusinessException("El equipo " + equipo.getCodigoInventario() + " está en estado " + equipo.getEstado() +
                    ". Solo se pueden prestar equipos DISPONIBLES.");
        }
    }

    private String codigos(Prestamo prestamo) {
        return prestamo.getDetalles().stream()
                .map(detalle -> detalle.getEquipo().getCodigoInventario())
                .collect(Collectors.joining(", "));
    }
}
