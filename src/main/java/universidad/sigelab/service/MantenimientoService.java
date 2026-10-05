package universidad.sigelab.service;

import universidad.sigelab.enums.AccionAuditoria;
import universidad.sigelab.enums.EstadoEquipo;
import universidad.sigelab.enums.EstadoMantenimiento;
import universidad.sigelab.enums.Modulo;
import universidad.sigelab.enums.ResultadoMantenimiento;
import universidad.sigelab.enums.TipoMantenimiento;
import universidad.sigelab.model.Equipo;
import universidad.sigelab.model.Mantenimiento;
import universidad.sigelab.repository.EquipoRepository;
import universidad.sigelab.repository.MantenimientoRepository;
import universidad.sigelab.util.BusinessException;
import universidad.sigelab.util.Sesion;
import universidad.sigelab.util.Transaccion;
import universidad.sigelab.util.Validador;

import java.time.LocalDateTime;
import java.util.List;

public class MantenimientoService {
    private static final String ENTIDAD = "Mantenimiento";

    private final MantenimientoRepository repository;
    private final EquipoRepository equipoRepository;
    private final AuditoriaService auditoriaService;
    public MantenimientoService() { this(new MantenimientoRepository(), new EquipoRepository(), new AuditoriaService()); }
    public MantenimientoService(MantenimientoRepository repository, EquipoRepository equipoRepository,
                                AuditoriaService auditoriaService) {
        this.repository = repository;
        this.equipoRepository = equipoRepository;
        this.auditoriaService = auditoriaService;
    }

    public Equipo buscarEquipo(String codigo) {
        Sesion.exigirAcceso(Modulo.MANTENIMIENTO);
        String codigoLimpio = Validador.requerido(codigo, "Código de equipo", 30);
        return equipoRepository.buscarPorCodigo(codigoLimpio)
                .orElseThrow(() -> new BusinessException("No existe un equipo con el código " + codigoLimpio + "."));
    }

    public List<Mantenimiento> listar(boolean soloEnProceso) {
        Sesion.exigirAcceso(Modulo.MANTENIMIENTO);
        return soloEnProceso ? repository.listarEnProceso() : repository.listar();
    }

    /**
     * Transiciones permitidas hacia MANTENIMIENTO:
     *   DAÑADO     → MANTENIMIENTO
     *   DISPONIBLE → MANTENIMIENTO (solo preventivo)
     * Un equipo PRESTADO, en MANTENIMIENTO o de BAJA no puede ingresar.
     */
    public Mantenimiento enviarAMantenimiento(String codigoEquipo, TipoMantenimiento tipo, String descripcion,
                                              String responsable) {
        Sesion.exigirAcceso(Modulo.MANTENIMIENTO);
        Equipo equipo = buscarEquipo(codigoEquipo);
        if (tipo == null) throw new BusinessException("Seleccione el tipo de mantenimiento.");
        String descripcionLimpia = Validador.requerido(descripcion, "Descripción", 500);
        String responsableLimpio = Validador.requerido(responsable, "Responsable", 150);

        EstadoEquipo estadoActual = equipo.getEstado();
        if (estadoActual != EstadoEquipo.DANADO && estadoActual != EstadoEquipo.DISPONIBLE) {
            throw new BusinessException("El equipo " + equipo.getCodigoInventario() + " está en estado " + estadoActual +
                    ". Solo se envían a mantenimiento equipos DAÑADOS o DISPONIBLES.");
        }
        if (estadoActual == EstadoEquipo.DISPONIBLE && tipo != TipoMantenimiento.PREVENTIVO) {
            throw new BusinessException("Un equipo DISPONIBLE solo puede enviarse a mantenimiento PREVENTIVO.");
        }

        Mantenimiento mantenimiento = new Mantenimiento(equipo, LocalDateTime.now(), tipo, descripcionLimpia,
                responsableLimpio, Sesion.getUsuarioActual());

        return Transaccion.ejecutar("enviar el equipo a mantenimiento", connection -> {
            boolean cambiado = equipoRepository.cambiarEstado(connection, equipo.getIdEquipo(),
                    estadoActual, EstadoEquipo.MANTENIMIENTO);
            if (!cambiado) {
                throw new BusinessException("El equipo " + equipo.getCodigoInventario() +
                        " cambió de estado. No se envió a mantenimiento.");
            }
            repository.guardar(connection, mantenimiento);
            equipo.setEstado(EstadoEquipo.MANTENIMIENTO);
            auditoriaService.registrar(connection, mantenimiento.getUsuarioRegistra(),
                    AccionAuditoria.EQUIPO_ENVIADO_MANTENIMIENTO, ENTIDAD, mantenimiento.getIdMantenimiento(),
                    "Equipo " + equipo.getCodigoInventario() + " enviado a mantenimiento " + tipo +
                            " (estaba " + estadoActual + ").");
            return mantenimiento;
        });
    }

    /** RN-15: al finalizar, el equipo queda DISPONIBLE (reparado) o de BAJA (no reparable). */
    public Mantenimiento finalizar(int idMantenimiento, ResultadoMantenimiento resultado, String observaciones) {
        Sesion.exigirAcceso(Modulo.MANTENIMIENTO);
        if (resultado == null) throw new BusinessException("Seleccione el resultado del mantenimiento.");
        String observacionesLimpias = Validador.opcional(observaciones, "Observaciones", 500);

        Mantenimiento mantenimiento = repository.buscarPorId(idMantenimiento)
                .orElseThrow(() -> new BusinessException("El mantenimiento ya no existe."));
        if (mantenimiento.getEstado() != EstadoMantenimiento.EN_PROCESO) {
            throw new BusinessException("El mantenimiento N° " + idMantenimiento + " ya fue finalizado.");
        }

        Equipo equipo = mantenimiento.getEquipo();
        EstadoEquipo nuevoEstado = resultado == ResultadoMantenimiento.REPARADO ? EstadoEquipo.DISPONIBLE : EstadoEquipo.BAJA;
        mantenimiento.setFechaSalida(LocalDateTime.now());
        mantenimiento.setResultado(resultado);
        mantenimiento.setEstado(EstadoMantenimiento.FINALIZADO);
        mantenimiento.setObservaciones(observacionesLimpias);

        return Transaccion.ejecutar("finalizar el mantenimiento", connection -> {
            if (!repository.finalizar(connection, mantenimiento)) {
                throw new BusinessException("El mantenimiento N° " + idMantenimiento + " ya fue finalizado.");
            }
            boolean cambiado = equipoRepository.cambiarEstado(connection, equipo.getIdEquipo(),
                    EstadoEquipo.MANTENIMIENTO, nuevoEstado);
            if (!cambiado) {
                throw new BusinessException("El equipo " + equipo.getCodigoInventario() +
                        " no está en MANTENIMIENTO. No se finalizó.");
            }
            equipo.setEstado(nuevoEstado);
            auditoriaService.registrar(connection, Sesion.getUsuarioActual(),
                    AccionAuditoria.MANTENIMIENTO_FINALIZADO, ENTIDAD, idMantenimiento,
                    "Mantenimiento del equipo " + equipo.getCodigoInventario() + " finalizado como " + resultado +
                            ". El equipo queda " + nuevoEstado + ".");
            return mantenimiento;
        });
    }
}
