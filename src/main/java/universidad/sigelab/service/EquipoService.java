package universidad.sigelab.service;

import universidad.sigelab.enums.EstadoEquipo;
import universidad.sigelab.enums.EstadoRegistro;
import universidad.sigelab.enums.Modulo;
import universidad.sigelab.model.Equipo;
import universidad.sigelab.repository.EquipoRepository;
import universidad.sigelab.repository.LaboratorioRepository;
import universidad.sigelab.repository.TipoEquipoRepository;
import universidad.sigelab.util.BusinessException;
import universidad.sigelab.util.Sesion;
import universidad.sigelab.util.Validador;

import java.util.List;
import java.util.Optional;

public class EquipoService {
    private final EquipoRepository repository;
    private final TipoEquipoRepository tipoEquipoRepository;
    private final LaboratorioRepository laboratorioRepository;
    public EquipoService() { this(new EquipoRepository(), new TipoEquipoRepository(), new LaboratorioRepository()); }
    public EquipoService(EquipoRepository repository, TipoEquipoRepository tipoEquipoRepository,
                         LaboratorioRepository laboratorioRepository) {
        this.repository = repository;
        this.tipoEquipoRepository = tipoEquipoRepository;
        this.laboratorioRepository = laboratorioRepository;
    }

    /** Todo equipo nuevo inicia DISPONIBLE; los demás estados son consecuencia de las operaciones. */
    public boolean guardar(Equipo equipo) {
        Sesion.exigirAcceso(Modulo.EQUIPOS);
        validar(equipo);
        exigirCodigoUnico(equipo.getCodigoInventario(), 0);
        equipo.setEstado(EstadoEquipo.DISPONIBLE);
        return repository.guardar(equipo);
    }

    /** Solo cambia los datos descriptivos: el estado no se edita desde el catálogo. */
    public boolean actualizar(Equipo equipo) {
        Sesion.exigirAcceso(Modulo.EQUIPOS);
        validar(equipo);
        if (repository.buscarPorId(equipo.getIdEquipo()).isEmpty()) throw new BusinessException("El equipo ya no existe.");
        exigirCodigoUnico(equipo.getCodigoInventario(), equipo.getIdEquipo());
        return repository.actualizar(equipo);
    }

    public List<Equipo> listar() { return repository.listar(); }
    public List<Equipo> listarPorEstado(EstadoEquipo estado) { return repository.listarPorEstado(estado); }
    public Optional<Equipo> buscar(int id) { return repository.buscarPorId(id); }

    public Optional<Equipo> buscarPorCodigo(String codigo) {
        return codigo == null || codigo.isBlank() ? Optional.empty() : repository.buscarPorCodigo(codigo.trim());
    }

    /** RN-04: el código de inventario debe ser único. */
    private void exigirCodigoUnico(String codigo, int idExcluido) {
        if (repository.existeCodigo(codigo, idExcluido)) {
            throw new BusinessException("Ya existe un equipo con el código " + codigo + ".");
        }
    }

    private void validar(Equipo equipo) {
        if (equipo == null) throw new BusinessException("El equipo es obligatorio.");
        equipo.setCodigoInventario(Validador.requerido(equipo.getCodigoInventario(), "Código de inventario", 30).toUpperCase());
        equipo.setNombre(Validador.requerido(equipo.getNombre(), "Nombre", 120));
        equipo.setMarca(Validador.requerido(equipo.getMarca(), "Marca", 60));
        equipo.setModelo(Validador.requerido(equipo.getModelo(), "Modelo", 60));
        equipo.setNumeroSerie(Validador.opcional(equipo.getNumeroSerie(), "Número de serie", 60));
        equipo.setObservaciones(Validador.opcional(equipo.getObservaciones(), "Observaciones", 500));

        // El tipo y el laboratorio deben ser registros reales y activos de sus catálogos.
        boolean tipoValido = equipo.getTipo() != null && tipoEquipoRepository.buscarPorId(equipo.getTipo().getIdTipoEquipo())
                .filter(tipo -> tipo.getEstado() == EstadoRegistro.ACTIVO).isPresent();
        if (!tipoValido) throw new BusinessException("Seleccione un tipo de equipo activo.");

        boolean laboratorioValido = equipo.getLaboratorio() != null
                && laboratorioRepository.buscarPorId(equipo.getLaboratorio().getIdLaboratorio())
                .filter(laboratorio -> laboratorio.getEstado() == EstadoRegistro.ACTIVO).isPresent();
        if (!laboratorioValido) throw new BusinessException("Seleccione un laboratorio activo.");
    }
}
