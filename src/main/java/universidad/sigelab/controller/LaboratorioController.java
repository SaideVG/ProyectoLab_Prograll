package universidad.sigelab.controller;

import universidad.sigelab.enums.EstadoRegistro;
import universidad.sigelab.model.Laboratorio;
import universidad.sigelab.service.LaboratorioService;

import java.util.List;
import java.util.Optional;

public class LaboratorioController {
    private final LaboratorioService service;
    public LaboratorioController() { this(new LaboratorioService()); }
    public LaboratorioController(LaboratorioService service) { this.service = service; }
    public boolean guardar(Laboratorio laboratorio) { return service.guardar(laboratorio); }
    public boolean actualizar(Laboratorio laboratorio) { return service.actualizar(laboratorio); }
    public boolean cambiarEstado(int id, EstadoRegistro nuevoEstado) { return service.cambiarEstado(id, nuevoEstado); }
    public List<Laboratorio> listar() { return service.listar(); }
    public List<Laboratorio> listarActivos() { return service.listarActivos(); }
    public Optional<Laboratorio> buscar(int id) { return service.buscar(id); }
    public Optional<Laboratorio> buscarPorCodigo(String codigo) { return service.buscarPorCodigo(codigo); }
}
