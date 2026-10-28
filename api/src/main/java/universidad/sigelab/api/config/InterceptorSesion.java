package universidad.sigelab.api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import universidad.sigelab.api.model.Usuario;
import universidad.sigelab.api.service.SesionService;

/**
 * Se ejecuta antes de cada petición protegida: lee el token del encabezado
 * "Authorization: Bearer <token>" y deja al usuario en la petición para que
 * los controllers sepan quién la hizo. Sin token válido responde 401.
 */
@Component
public class InterceptorSesion implements HandlerInterceptor {

    public static final String ATRIBUTO_USUARIO = "usuarioSesion";
    private static final String PREFIJO = "Bearer ";

    private final SesionService sesionService;

    public InterceptorSesion(SesionService sesionService) {
        this.sesionService = sesionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // El navegador pregunta con OPTIONS antes de llamar (CORS); esa consulta no lleva token.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        Usuario usuario = sesionService.usuarioDelToken(token(request));
        request.setAttribute(ATRIBUTO_USUARIO, usuario);
        return true;
    }

    public static String token(HttpServletRequest request) {
        String encabezado = request.getHeader("Authorization");
        if (encabezado == null || !encabezado.startsWith(PREFIJO)) return null;
        return encabezado.substring(PREFIJO.length()).trim();
    }
}
