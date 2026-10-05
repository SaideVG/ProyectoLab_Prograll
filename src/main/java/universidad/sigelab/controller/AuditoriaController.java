package universidad.sigelab.controller;

import universidad.sigelab.enums.AccionAuditoria;
import universidad.sigelab.model.Auditoria;
import universidad.sigelab.service.AuditoriaService;

import java.util.List;

public class AuditoriaController {
    private final AuditoriaService service;
    public AuditoriaController() { this(new AuditoriaService()); }
    public AuditoriaController(AuditoriaService service) { this.service = service; }
    public List<Auditoria> listar(AccionAuditoria accion) { return service.listar(accion); }
}
