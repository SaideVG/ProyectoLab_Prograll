package universidad.sigelab.api.util;

/** El rol del usuario no tiene permiso para el módulo. HTTP 403. */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
