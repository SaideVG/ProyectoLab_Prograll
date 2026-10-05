package universidad.sigelab.service;

import universidad.sigelab.enums.EstadoRegistro;
import universidad.sigelab.enums.Modulo;
import universidad.sigelab.model.Laboratorio;
import universidad.sigelab.repository.LaboratorioRepository;
import universidad.sigelab.util.BusinessException;
import universidad.sigelab.util.Sesion;
import universidad.sigelab.util.Validador;

import java.util.List;
import java.util.Optional;

public class LaboratorioService {
    private final LaboratorioRepository repository;
    public LaboratorioService() { this(new LaboratorioRepository()); }
    public LaboratorioService(LaboratorioRepository repository) { this.repository = repository; }

    public boolean guardar(Laboratorio laboratorio) {
        Sesion.exigirAcceso(Modulo.LABORATORIOS);
        validar(laboratorio);
        exigirCodigoUnico(laboratorio.getCodigo(), 0);
        laboratorio.setEstado(EstadoRegistro.ACTIVO);
        return repository.guardar(laboratorio);
    }

    public boolean actualizar(Laboratorio laboratorio) {
        Sesion.exigirAcceso(Modulo.LABORATORIOS);
        validar(laboratorio);
        Laboratorio actual = repository.buscarPorId(laboratorio.getIdLaboratorio())
                .orElseThrow(() -> new BusinessException("El laboratorio ya no existe."));
        exigirCodigoUnico(laboratorio.getCodigo(), laboratorio.getIdLaboratorio());
        // El estado solo cambia con cambiarEstado().
        laboratorio.setEstado(actual.getEstado());
        return repository.actualizar(laboratorio);
    }

    public boolean cambiarEstado(int idLaboratorio, EstadoRegistro nuevoEstado) {
        Sesion.exigirAcceso(Modulo.LABORATORIOS);
        Laboratorio laboratorio = repository.buscarPorId(idLaboratorio)
                .orElseThrow(() -> new BusinessException("El laboratorio ya no existe."));
        laboratorio.setEstado(nuevoEstado);
        return repository.actualizar(laboratorio);
    }

    public List<Laboratorio> listar() { return repository.listar(); }
    public List<Laboratorio> listarActivos() { return repository.listarActivos(); }
    public Optional<Laboratorio> buscar(int id) { return repository.buscarPorId(id); }
    public Optional<Laboratorio> buscarPorCodigo(String codigo) { return repository.buscarPorCodigo(codigo); }

    /** RN-03: el código de laboratorio debe ser único. */
    private void exigirCodigoUnico(String codigo, int idExcluido) {
        if (repository.existeCodigo(codigo, idExcluido)) {
            throw new BusinessException("Ya existe un laboratorio con el código " + codigo + ".");
        }
    }

    private void validar(Laboratorio laboratorio) {
        if (laboratorio == null) throw new BusinessException("El laboratorio es obligatorio.");
        laboratorio.setCodigo(Validador.requerido(laboratorio.getCodigo(), "Código", 20).toUpperCase());
        laboratorio.setNombre(Validador.requerido(laboratorio.getNombre(), "Nombre", 100));
        laboratorio.setUbicacion(Validador.requerido(laboratorio.getUbicacion(), "Ubicación", 200));
        if (laboratorio.getCapacidad() <= 0) throw new BusinessException("La capacidad debe ser mayor que cero.");
    }
}
