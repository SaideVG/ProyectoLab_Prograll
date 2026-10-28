package universidad.sigelab.api.service;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import universidad.sigelab.api.dto.LoginRequest;
import universidad.sigelab.api.dto.LoginResponse;
import universidad.sigelab.api.model.Usuario;
import universidad.sigelab.api.repository.AuditoriaRepository;
import universidad.sigelab.api.repository.UsuarioRepository;
import universidad.sigelab.api.util.BusinessException;

/**
 * Inicio de sesión desde la app móvil.
 *
 * Aplica las mismas reglas que UsuarioService del escritorio: contraseña con
 * BCrypt, RN-01 (usuario ACTIVO) y registro en Auditoría de cada intento.
 * La validación facial (RN-02) se queda en el escritorio, que es donde está la
 * cámara de rostro; la app es un complemento de consulta.
 */
@Service
public class UsuarioService {

    private static final String ENTIDAD = "Usuario";
    private static final String CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos.";

    private final UsuarioRepository usuarioRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final SesionService sesionService;
    private final ControlIntentosService controlIntentos;

    public UsuarioService(UsuarioRepository usuarioRepository, AuditoriaRepository auditoriaRepository,
                          SesionService sesionService, ControlIntentosService controlIntentos) {
        this.usuarioRepository = usuarioRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.sesionService = sesionService;
        this.controlIntentos = controlIntentos;
    }

    public LoginResponse iniciarSesion(LoginRequest datos) {
        String nombreUsuario = datos == null ? null : datos.usuario();
        String password = datos == null ? null : datos.password();

        if (nombreUsuario == null || nombreUsuario.isBlank()) throw new BusinessException("Ingrese su usuario.");
        if (password == null || password.isEmpty()) throw new BusinessException("Ingrese su contraseña.");

        String nombreLimpio = nombreUsuario.trim();

        // Los intentos mientras dura el bloqueo no se auditan uno por uno, para que
        // alguien probando contraseñas no llene la tabla Auditoria; sí queda el momento del bloqueo.
        controlIntentos.exigirNoBloqueado(nombreLimpio);

        Usuario usuario = usuarioRepository.buscarPorUsuario(nombreLimpio).orElse(null);
        if (usuario == null) {
            controlIntentos.registrarFallo(nombreLimpio);
            auditoriaRepository.registrar(null, "LOGIN_FALLIDO", ENTIDAD, null,
                    "Usuario inexistente (app móvil): " + nombreLimpio);
            throw new BusinessException(CREDENCIALES_INVALIDAS);
        }

        if (!passwordCorrecta(password, usuario.passwordHash())) {
            boolean bloqueado = controlIntentos.registrarFallo(nombreLimpio);
            registrarFallido(usuario, bloqueado
                    ? "Contraseña incorrecta; usuario bloqueado " + controlIntentos.minutosBloqueo() + " minutos en la app móvil."
                    : "Contraseña incorrecta (app móvil).");
            throw new BusinessException(CREDENCIALES_INVALIDAS);
        }

        // RN-01. Se revisa después de la contraseña para no revelar el estado de una cuenta a quien no la conoce.
        if (!usuario.activo()) {
            registrarFallido(usuario, "Usuario inactivo (app móvil).");
            throw new BusinessException("El usuario está INACTIVO. Contacte al administrador.");
        }

        controlIntentos.registrarExito(nombreLimpio);
        auditoriaRepository.registrar(usuario.id(), "LOGIN_EXITOSO", ENTIDAD, usuario.id(),
                "Inicio de sesión desde la app móvil.");

        String token = sesionService.crear(usuario);
        return new LoginResponse(token, sesionService.expiracion(token), usuario.aDto());
    }

    public void cerrarSesion(String token) {
        sesionService.cerrar(token);
    }

    private void registrarFallido(Usuario usuario, String motivo) {
        auditoriaRepository.registrar(usuario.id(), "LOGIN_FALLIDO", ENTIDAD, usuario.id(), motivo);
    }

    private boolean passwordCorrecta(String password, String hash) {
        try { return BCrypt.checkpw(password, hash); }
        catch (IllegalArgumentException e) { return false; }
    }
}
