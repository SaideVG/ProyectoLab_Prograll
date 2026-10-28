package universidad.sigelab.api.util;

/**
 * Error de una regla de negocio o de un dato inválido.
 * Igual que en el escritorio: su mensaje se le muestra tal cual al usuario.
 * La API lo responde con el código HTTP 400.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String mensaje) {
        super(mensaje);
    }
}
