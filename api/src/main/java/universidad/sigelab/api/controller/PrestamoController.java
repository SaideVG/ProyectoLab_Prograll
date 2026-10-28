package universidad.sigelab.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import universidad.sigelab.api.config.InterceptorSesion;
import universidad.sigelab.api.dto.PrestamoDetalleDto;
import universidad.sigelab.api.dto.PrestamoDto;
import universidad.sigelab.api.enums.Modulo;
import universidad.sigelab.api.model.Usuario;
import universidad.sigelab.api.service.ReporteService;
import universidad.sigelab.api.service.SesionService;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final ReporteService reporteService;
    private final SesionService sesionService;

    public PrestamoController(ReporteService reporteService, SesionService sesionService) {
        this.reporteService = reporteService;
        this.sesionService = sesionService;
    }

    /** GET /api/prestamos?filtro=ACTIVOS | VENCIDOS | FINALIZADOS */
    @GetMapping
    public List<PrestamoDto> listar(@RequestAttribute(InterceptorSesion.ATRIBUTO_USUARIO) Usuario usuario,
                                    @RequestParam(required = false) String filtro) {
        sesionService.exigirAcceso(usuario, Modulo.REPORTES);
        return reporteService.listarPrestamos(filtro);
    }

    /** GET /api/prestamos/5: el préstamo con sus equipos y el estado de devolución de cada uno. */
    @GetMapping("/{id}")
    public PrestamoDetalleDto detalle(@RequestAttribute(InterceptorSesion.ATRIBUTO_USUARIO) Usuario usuario,
                                      @PathVariable int id) {
        sesionService.exigirAcceso(usuario, Modulo.REPORTES);
        return reporteService.detalleDePrestamo(id);
    }
}
