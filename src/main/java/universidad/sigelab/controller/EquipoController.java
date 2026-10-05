package universidad.sigelab.controller;

import universidad.sigelab.enums.EstadoEquipo;
import universidad.sigelab.model.Equipo;
import universidad.sigelab.service.EquipoService;

import java.util.List;
import java.util.Optional;

public class EquipoController {
    private final EquipoService service;
    public EquipoController() { this(new EquipoService()); }
    public EquipoController(EquipoService service) { this.service = service; }
    public boolean guardar(Equipo equipo) { return service.guardar(equipo); }
    public boolean actualizar(Equipo equipo) { return service.actualizar(equipo); }
    public List<Equipo> listar() { return service.listar(); }
    public List<Equipo> listarPorEstado(EstadoEquipo estado) { return service.listarPorEstado(estado); }
    public Optional<Equipo> buscar(int id) { return service.buscar(id); }
    public Optional<Equipo> buscarPorCodigo(String codigo) { return service.buscarPorCodigo(codigo); }
}
