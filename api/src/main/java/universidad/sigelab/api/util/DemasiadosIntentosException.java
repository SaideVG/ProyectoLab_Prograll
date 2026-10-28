package universidad.sigelab.api.util;

/** El usuario está bloqueado por varios intentos fallidos de login seguidos. HTTP 429. */
public class DemasiadosIntentosException extends BusinessException {

    public DemasiadosIntentosException(String mensaje) {
        super(mensaje);
    }
}
