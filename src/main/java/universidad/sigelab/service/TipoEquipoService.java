package universidad.sigelab.service;

import universidad.sigelab.enums.EstadoRegistro;
import universidad.sigelab.enums.Modulo;
import universidad.sigelab.model.TipoEquipo;
import universidad.sigelab.repository.TipoEquipoRepository;
import universidad.sigelab.util.BusinessException;
import universidad.sigelab.util.Sesion;
import universidad.sigelab.util.Validador;

import java.util.List;
import java.util.Optional;

public class TipoEquipoService {
    private final TipoEquipoRepository repository;
    public TipoEquipoService() { this(new TipoEquipoRepository()); }
    public TipoEquipoService(TipoEquipoRepository repository) { this.repository = repository; }

    public boolean guardar(TipoEquipo tipo) {
        Sesion.exigirAcceso(Modulo.TIPOS_EQUIPO);
        validar(tipo);
        exigirNombreUnico(tipo.getNombre(), 0);
        tipo.setEstado(EstadoRegistro.ACTIVO);
        return repository.guardar(tipo);
    }

    public boolean actualizar(TipoEquipo tipo) {
        Sesion.exigirAcceso(Modulo.TIPOS_EQUIPO);
        validar(tipo);
        TipoEquipo actual = repository.buscarPorId(tipo.getIdTipoEquipo())
                .orElseThrow(() -> new BusinessException("El tipo de equipo ya no existe."));
        exigirNombreUnico(tipo.getNombre(), tipo.getIdTipoEquipo());
        // El estado solo cambia con cambiarEstado().
        tipo.setEstado(actual.getEstado());
        return repository.actualizar(tipo);
    }

    public boolean cambiarEstado(int idTipoEquipo, EstadoRegistro nuevoEstado) {
        Sesion.exigirAcceso(Modulo.TIPOS_EQUIPO);
        TipoEquipo tipo = repository.buscarPorId(idTipoEquipo)
                .orElseThrow(() -> new BusinessException("El tipo de equipo ya no existe."));
        tipo.setEstado(nuevoEstado);
        return repository.actualizar(tipo);
    }

    public List<TipoEquipo> listar() { return repository.listar(); }
    public List<TipoEquipo> listarActivos() { return repository.listarActivos(); }
    public Optional<TipoEquipo> buscar(int id) { return repository.buscarPorId(id); }
    public Optional<TipoEquipo> buscarPorNombre(String nombre) { return repository.buscarPorNombre(nombre); }

    private void exigirNombreUnico(String nombre, int idExcluido) {
        if (repository.existeNombre(nombre, idExcluido)) {
            throw new BusinessException("Ya existe un tipo de equipo llamado " + nombre + ".");
        }
    }

    private void validar(TipoEquipo tipo) {
        if (tipo == null) throw new BusinessException("El tipo de equipo es obligatorio.");
        tipo.setNombre(Validador.requerido(tipo.getNombre(), "Nombre", 60));
        tipo.setDescripcion(Validador.opcional(tipo.getDescripcion(), "Descripción", 200));
    }
}
