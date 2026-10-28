package universidad.sigelab.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import universidad.sigelab.api.util.DemasiadosIntentosException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita los intentos de inicio de sesión desde la app.
 *
 * Cuando la API se publica en internet (por ejemplo con un túnel), cualquiera
 * que conozca la dirección podría probar contraseñas sin parar. Por eso, después
 * de varios intentos fallidos seguidos con el mismo usuario, ese usuario queda
 * bloqueado unos minutos. Solo afecta a la app: el escritorio no usa la API.
 * Un inicio de sesión correcto vuelve a empezar la cuenta de fallos.
 *
 * Igual que los tokens, la cuenta vive en memoria: si la API se reinicia, empieza de cero.
 */
@Service
public class ControlIntentosService {

    /** Fallos seguidos de un usuario y, si se bloqueó, hasta cuándo. */
    private record Registro(int fallos, LocalDateTime bloqueadoHasta) { }

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();
    private final int maximoIntentos;
    private final long minutosBloqueo;

    public ControlIntentosService(@Value("${sigelab.login.max-intentos:5}") int maximoIntentos,
                                  @Value("${sigelab.login.minutos-bloqueo:15}") long minutosBloqueo) {
        this.maximoIntentos = maximoIntentos;
        this.minutosBloqueo = minutosBloqueo;
    }

    public long minutosBloqueo() {
        return minutosBloqueo;
    }

    /** Se llama antes de revisar la contraseña: si el usuario está bloqueado, ni siquiera se compara. */
    public void exigirNoBloqueado(String nombreUsuario) {
        Registro registro = registros.get(clave(nombreUsuario));
        if (registro == null || registro.bloqueadoHasta() == null) return;

        LocalDateTime ahora = LocalDateTime.now();
        if (registro.bloqueadoHasta().isAfter(ahora)) {
            long segundos = Duration.between(ahora, registro.bloqueadoHasta()).toSeconds();
            long minutos = Math.max(1, (segundos + 59) / 60);
            throw new DemasiadosIntentosException("Demasiados intentos fallidos. Intente de nuevo en "
                    + minutos + (minutos == 1 ? " minuto." : " minutos."));
        }

        // El bloqueo ya terminó: la cuenta empieza de cero.
        registros.remove(clave(nombreUsuario));
    }

    /** Suma un fallo. Devuelve true si con este fallo el usuario quedó bloqueado. */
    public boolean registrarFallo(String nombreUsuario) {
        Registro actualizado = registros.merge(clave(nombreUsuario), new Registro(1, null), (anterior, nuevo) -> {
            int fallos = anterior.fallos() + 1;
            LocalDateTime bloqueo = fallos >= maximoIntentos ? LocalDateTime.now().plusMinutes(minutosBloqueo) : null;
            return new Registro(fallos, bloqueo);
        });
        return actualizado.bloqueadoHasta() != null;
    }

    public void registrarExito(String nombreUsuario) {
        registros.remove(clave(nombreUsuario));
    }

    /** "Admin" y "admin " son el mismo usuario para el conteo. */
    private String clave(String nombreUsuario) {
        return nombreUsuario.trim().toLowerCase();
    }
}
