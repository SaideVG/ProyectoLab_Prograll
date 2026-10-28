package universidad.sigelab.api.util;

/** Falta el token, es inválido o ya venció: la app debe volver al login. HTTP 401. */
public class NoAutenticadoException extends RuntimeException {

    public NoAutenticadoException(String mensaje) {
        super(mensaje);
    }
}
