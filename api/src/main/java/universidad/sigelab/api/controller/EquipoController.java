package universidad.sigelab.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import universidad.sigelab.api.config.InterceptorSesion;
import universidad.sigelab.api.dto.EquipoDetalleDto;
import universidad.sigelab.api.dto.EquipoDto;
import universidad.sigelab.api.enums.Modulo;
import universidad.sigelab.api.model.Usuario;
import universidad.sigelab.api.service.ReporteService;
import universidad.sigelab.api.service.SesionService;

import java.util.List;

@RestController
@RequestMapping("/api/equipos")
public class EquipoController {

    private final ReporteService reporteService;
    private final SesionService sesionService;

    public EquipoController(ReporteService reporteService, SesionService sesionService) {
        this.reporteService = reporteService;
        this.sesionService = sesionService;
    }

    /** GET /api/equipos?estado=DISPONIBLE&buscar=osc  (los dos filtros son opcionales) */
    @GetMapping
    public List<EquipoDto> buscar(@RequestAttribute(InterceptorSesion.ATRIBUTO_USUARIO) Usuario usuario,
                                  @RequestParam(required = false) String estado,
                                  @RequestParam(required = false) String buscar) {
        sesionService.exigirAcceso(usuario, Modulo.REPORTES);
        return reporteService.buscarEquipos(estado, buscar);
    }

    /** GET /api/equipos/OSC-001: datos, préstamos y mantenimientos del equipo (lo que se ve al escanear su QR). */
    @GetMapping("/{codigo}")
    public EquipoDetalleDto detalle(@RequestAttribute(InterceptorSesion.ATRIBUTO_USUARIO) Usuario usuario,
                                    @PathVariable String codigo) {
        sesionService.exigirAcceso(usuario, Modulo.REPORTES);
        return reporteService.detalleDeEquipo(codigo);
    }
}
