package universidad.sigelab.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import universidad.sigelab.api.config.InterceptorSesion;
import universidad.sigelab.api.dto.MantenimientoDto;
import universidad.sigelab.api.enums.Modulo;
import universidad.sigelab.api.model.Usuario;
import universidad.sigelab.api.service.ReporteService;
import universidad.sigelab.api.service.SesionService;

import java.util.List;

@RestController
public class MantenimientoController {

    private final ReporteService reporteService;
    private final SesionService sesionService;

    public MantenimientoController(ReporteService reporteService, SesionService sesionService) {
        this.reporteService = reporteService;
        this.sesionService = sesionService;
    }

    /** GET /api/mantenimientos?soloEnProceso=true */
    @GetMapping("/api/mantenimientos")
    public List<MantenimientoDto> listar(@RequestAttribute(InterceptorSesion.ATRIBUTO_USUARIO) Usuario usuario,
                                         @RequestParam(defaultValue = "true") boolean soloEnProceso) {
        sesionService.exigirAcceso(usuario, Modulo.REPORTES);
        return reporteService.listarMantenimientos(soloEnProceso);
    }
}
