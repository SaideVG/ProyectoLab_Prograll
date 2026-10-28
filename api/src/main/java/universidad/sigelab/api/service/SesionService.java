package universidad.sigelab.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import universidad.sigelab.api.enums.Modulo;
import universidad.sigelab.api.model.Usuario;
import universidad.sigelab.api.repository.UsuarioRepository;
import universidad.sigelab.api.util.AccesoDenegadoException;
import universidad.sigelab.api.util.NoAutenticadoException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sesiones de la app móvil.
 *
 * En el escritorio la sesión es una sola (la clase Sesion guarda al usuario
 * que inició sesión en esa computadora). La API, en cambio, atiende a muchos
 * celulares a la vez, así que cada inicio de sesión recibe un token distinto:
 * un texto aleatorio que la app envía en cada petición para decir quién es.
 *
 * Los tokens se guardan en memoria: si la API se reinicia, los usuarios
 * simplemente vuelven a iniciar sesión.
 */
@Service
public class SesionService {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    /** Lo que se guarda por cada token: de quién es y hasta cuándo vale. */
    public record SesionActiva(int idUsuario, LocalDateTime expira) { }

    private final Map<String, SesionActiva> sesiones = new ConcurrentHashMap<>();
    private final UsuarioRepository usuarioRepository;
    private final long horasDeSesion;

    public SesionService(UsuarioRepository usuarioRepository, @Value("${sigelab.sesion.horas:12}") long horasDeSesion) {
        this.usuarioRepository = usuarioRepository;
        this.horasDeSesion = horasDeSesion;
    }

    public String crear(Usuario usuario) {
        quitarVencidas();

        // 32 bytes aleatorios = 256 bits: imposible de adivinar.
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        sesiones.put(token, new SesionActiva(usuario.id(), LocalDateTime.now().plusHours(horasDeSesion)));
        return token;
    }

    public LocalDateTime expiracion(String token) {
        SesionActiva sesion = sesiones.get(token);
        return sesion == null ? null : sesion.expira();
    }

    /**
     * Devuelve al dueño del token. Se lee de nuevo de la base en cada petición:
     * si un administrador lo desactiva desde el escritorio, pierde el acceso al instante.
     */
    public Usuario usuarioDelToken(String token) {
        SesionActiva sesion = token == null ? null : sesiones.get(token);
        if (sesion == null) throw new NoAutenticadoException("Inicie sesión para continuar.");

        if (sesion.expira().isBefore(LocalDateTime.now())) {
            sesiones.remove(token);
            throw new NoAutenticadoException("Su sesión venció. Inicie sesión de nuevo.");
        }

        Usuario usuario = usuarioRepository.buscarPorId(sesion.idUsuario()).orElse(null);
        if (usuario == null || !usuario.activo()) {
            sesiones.remove(token);
            throw new NoAutenticadoException("Su usuario ya no está activo. Contacte al administrador.");
        }
        return usuario;
    }

    public void cerrar(String token) {
        if (token != null) sesiones.remove(token);
    }

    /** Mismo control que Sesion.exigirAcceso del escritorio. */
    public void exigirAcceso(Usuario usuario, Modulo modulo) {
        if (!usuario.rol().puedeAcceder(modulo)) {
            throw new AccesoDenegadoException("Su rol (" + usuario.rol() + ") no tiene acceso a " + modulo + ".");
        }
    }

    private void quitarVencidas() {
        LocalDateTime ahora = LocalDateTime.now();
        sesiones.values().removeIf(sesion -> sesion.expira().isBefore(ahora));
    }
}
