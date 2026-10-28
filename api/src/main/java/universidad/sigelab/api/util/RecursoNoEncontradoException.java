package universidad.sigelab.api.util;

/** Lo que se pidió no existe (por ejemplo, un código de equipo). HTTP 404. */
public class RecursoNoEncontradoException extends BusinessException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
