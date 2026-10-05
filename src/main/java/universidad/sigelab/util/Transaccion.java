package universidad.sigelab.util;

import universidad.sigelab.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Ejecuta varias operaciones de base de datos como una sola: se guardan todas o ninguna.
 * Los Services la usan cuando una operación modifica varias tablas
 * (préstamo, devolución, mantenimiento).
 */
public final class Transaccion {
    private Transaccion() { }

    /** Pasos que deben ejecutarse sobre la misma conexión. */
    @FunctionalInterface
    public interface Operacion<T> {
        T ejecutar(Connection connection) throws SQLException;
    }

    public static <T> T ejecutar(String descripcion, Operacion<T> operacion) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(connection);
                connection.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                // Si cualquier paso falla (error de SQL o regla de negocio), se deshace todo lo anterior.
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw JdbcSupport.error(descripcion, e);
        }
    }
}
