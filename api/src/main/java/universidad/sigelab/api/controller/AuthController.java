package universidad.sigelab.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import universidad.sigelab.api.config.InterceptorSesion;
import universidad.sigelab.api.dto.LoginRequest;
import universidad.sigelab.api.dto.LoginResponse;
import universidad.sigelab.api.dto.UsuarioDto;
import universidad.sigelab.api.model.Usuario;
import universidad.sigelab.api.service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** POST /api/auth/login  {"usuario": "...", "password": "..."} */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody(required = false) LoginRequest datos) {
        return usuarioService.iniciarSesion(datos);
    }

    /** Datos del dueño del token: la app lo consulta al abrirse para saber si la sesión sigue viva. */
    @GetMapping("/yo")
    public UsuarioDto yo(@RequestAttribute(InterceptorSesion.ATRIBUTO_USUARIO) Usuario usuario) {
        return usuario.aDto();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        usuarioService.cerrarSesion(InterceptorSesion.token(request));
        return ResponseEntity.noContent().build();
    }
}
