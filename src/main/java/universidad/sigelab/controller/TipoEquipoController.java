package universidad.sigelab.controller;

import universidad.sigelab.enums.EstadoRegistro;
import universidad.sigelab.model.TipoEquipo;
import universidad.sigelab.service.TipoEquipoService;

import java.util.List;
import java.util.Optional;

public class TipoEquipoController {
    private final TipoEquipoService service;
    public TipoEquipoController() { this(new TipoEquipoService()); }
    public TipoEquipoController(TipoEquipoService service) { this.service = service; }
    public boolean guardar(TipoEquipo tipo) { return service.guardar(tipo); }
    public boolean actualizar(TipoEquipo tipo) { return service.actualizar(tipo); }
    public boolean cambiarEstado(int id, EstadoRegistro nuevoEstado) { return service.cambiarEstado(id, nuevoEstado); }
    public List<TipoEquipo> listar() { return service.listar(); }
    public List<TipoEquipo> listarActivos() { return service.listarActivos(); }
    public Optional<TipoEquipo> buscar(int id) { return service.buscar(id); }
    public Optional<TipoEquipo> buscarPorNombre(String nombre) { return service.buscarPorNombre(nombre); }
}
