package universidad.sigelab.util;

import universidad.sigelab.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Utilidades para que todos los repositorios ejecuten SQL y manejen los errores de la misma forma. */
public final class JdbcSupport {
    private static final int LOGIN_RECHAZADO = 18456;
    private static final int BASE_NO_DISPONIBLE = 4060;

    private JdbcSupport() { }

    /** Convierte la fila actual de un ResultSet en un objeto del modelo. */
    @FunctionalInterface
    public interface Mapeador<T> {
        T mapear(ResultSet resultSet) throws SQLException;
    }

    /*
     * ========================================================
     * OPERACIONES CON CONEXIÓN PROPIA
     * Cada llamada abre su conexión, ejecuta una sentencia y la cierra.
     * ========================================================
     */

    public static <T> List<T> consultar(String operacion, String sql, Mapeador<T> mapeador, Object... parametros) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            asignar(statement, parametros);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<T> resultado = new ArrayList<>();
                while (resultSet.next()) resultado.add(mapeador.mapear(resultSet));
                return resultado;
            }
        } catch (SQLException e) {
            throw error(operacion, e);
        }
    }

    public static <T> Optional<T> buscar(String operacion, String sql, Mapeador<T> mapeador, Object... parametros) {
        return consultar(operacion, sql, mapeador, parametros).stream().findFirst();
    }

    public static boolean existe(String operacion, String sql, Object... parametros) {
        return !consultar(operacion, sql, resultSet -> 1, parametros).isEmpty();
    }

    /** Para consultas SELECT COUNT(*) que devuelven una sola fila con un número. */
    public static int contar(String operacion, String sql, Object... parametros) {
        return consultar(operacion, sql, resultSet -> resultSet.getInt(1), parametros).get(0);
    }

    /** Ejecuta un INSERT y devuelve el ID generado por SQL Server. */
    public static int insertar(String operacion, String sql, Object... parametros) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return insertar(connection, sql, parametros);
        } catch (SQLException e) {
            throw error(operacion, e);
        }
    }

    /** Ejecuta un INSERT, UPDATE o DELETE y devuelve la cantidad de filas afectadas. */
    public static int actualizar(String operacion, String sql, Object... parametros) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return actualizar(connection, sql, parametros);
        } catch (SQLException e) {
            throw error(operacion, e);
        }
    }

    /*
     * ========================================================
     * OPERACIONES DENTRO DE UNA TRANSACCIÓN
     * Reciben la conexión que abrió Transaccion y no la cierran:
     * el commit o el rollback se decide al terminar toda la operación.
     * ========================================================
     */

    public static int insertar(Connection connection, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignar(statement, parametros);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) throw new SQLException("La base de datos no devolvió el ID generado.");
                return generatedKeys.getInt(1);
            }
        }
    }

    public static int actualizar(Connection connection, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            asignar(statement, parametros);
            return statement.executeUpdate();
        }
    }

    public static int contar(Connection connection, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            asignar(statement, parametros);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    /*
     * ========================================================
     * APOYO
     * ========================================================
     */

    public static DataAccessException error(String operation, SQLException cause) {
        if (esFalloDeConexion(cause)) {
            return new DataAccessException("No se pudo conectar con la base de datos. " +
                    "Verifique que SQL Server esté iniciado y que la base SigelabDB exista.", cause);
        }
        return new DataAccessException("No se pudo " + operation + ".", cause);
    }

    public static LocalDate localDate(ResultSet resultSet, String column) throws SQLException {
        return resultSet.getObject(column, LocalDate.class);
    }

    public static LocalDateTime localDateTime(ResultSet resultSet, String column) throws SQLException {
        return resultSet.getObject(column, LocalDateTime.class);
    }

    /** Los enums se guardan con name(), que es el texto que validan los CHECK de la base. */
    private static void asignar(PreparedStatement statement, Object... parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            Object parametro = parametros[i];
            if (parametro instanceof Enum<?> valor) statement.setString(i + 1, valor.name());
            else statement.setObject(i + 1, parametro);
        }
    }

    /** Los SQLState de clase 08 son errores de conexión; los otros dos códigos son propios de SQL Server. */
    private static boolean esFalloDeConexion(SQLException e) {
        String estado = e.getSQLState();
        return (estado != null && estado.startsWith("08"))
                || e.getErrorCode() == LOGIN_RECHAZADO
                || e.getErrorCode() == BASE_NO_DISPONIBLE;
    }
}
