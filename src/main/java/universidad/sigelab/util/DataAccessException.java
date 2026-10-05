package universidad.sigelab.util;

/** Error técnico producido al acceder a la base de datos. */
public class DataAccessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
