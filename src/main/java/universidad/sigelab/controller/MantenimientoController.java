package universidad.sigelab.controller;

import universidad.sigelab.enums.ResultadoMantenimiento;
import universidad.sigelab.enums.TipoMantenimiento;
import universidad.sigelab.model.Equipo;
import universidad.sigelab.model.Mantenimiento;
import universidad.sigelab.service.MantenimientoService;

import java.util.List;

public class MantenimientoController {
    private final MantenimientoService service;
    public MantenimientoController() { this(new MantenimientoService()); }
    public MantenimientoController(MantenimientoService service) { this.service = service; }
    public Equipo buscarEquipo(String codigo) { return service.buscarEquipo(codigo); }
    public List<Mantenimiento> listar(boolean soloEnProceso) { return service.listar(soloEnProceso); }
    public Mantenimiento enviarAMantenimiento(String codigoEquipo, TipoMantenimiento tipo, String descripcion, String responsable) { return service.enviarAMantenimiento(codigoEquipo, tipo, descripcion, responsable); }
    public Mantenimiento finalizar(int idMantenimiento, ResultadoMantenimiento resultado, String observaciones) { return service.finalizar(idMantenimiento, resultado, observaciones); }
}
