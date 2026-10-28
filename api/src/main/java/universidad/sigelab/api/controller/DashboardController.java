package universidad.sigelab.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import universidad.sigelab.api.config.InterceptorSesion;
import universidad.sigelab.api.dto.DashboardDto;
import universidad.sigelab.api.enums.Modulo;
import universidad.sigelab.api.model.Usuario;
import universidad.sigelab.api.service.ReporteService;
import universidad.sigelab.api.service.SesionService;

@RestController
public class DashboardController {

    private final ReporteService reporteService;
    private final SesionService sesionService;

    public DashboardController(ReporteService reporteService, SesionService sesionService) {
        this.reporteService = reporteService;
        this.sesionService = sesionService;
    }

    /** GET /api/dashboard: las cantidades del menú principal. */
    @GetMapping("/api/dashboard")
    public DashboardDto dashboard(@RequestAttribute(InterceptorSesion.ATRIBUTO_USUARIO) Usuario usuario) {
        sesionService.exigirAcceso(usuario, Modulo.REPORTES);
        return reporteService.obtenerDashboard();
    }
}
