package universidad.sigelab.repository;

import universidad.sigelab.util.JdbcSupport;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Acceso a la tabla TokenRecuperacion.
 *
 * Guarda únicamente el hash SHA-256 del código (nunca el código en claro), con
 * una fecha de expiración y la marca "usado" para que cada código sirva una sola
 * vez. Un mismo usuario puede tener varios códigos si vuelve a solicitarlos; al
 * pedir uno nuevo se anulan los anteriores.
 */
public class TokenRecuperacionRepository {

    private static final String CONSUMIR = """
            UPDATE TokenRecuperacion SET usado = 1
            WHERE id_usuario = ? AND codigo_hash = ? AND usado = 0 AND fecha_expiracion >= SYSDATETIME()
            """;

    /** Anula los códigos pendientes del usuario: al pedir uno nuevo, solo vale el último. */
    public void invalidarPendientes(int idUsuario) {
        JdbcSupport.actualizar("invalidar los códigos anteriores",
                "UPDATE TokenRecuperacion SET usado = 1 WHERE id_usuario = ? AND usado = 0", idUsuario);
    }

    public int crear(int idUsuario, byte[] hash, int minutosVigencia) {
        // La expiración se calcula con el reloj de SQL Server (SYSDATETIME) y no con el de
        // la máquina donde corre la aplicación: así la comparación de "no vencido" siempre
        // usa el mismo reloj, aunque el servidor y la aplicación estén en zonas horarias
        // distintas (por ejemplo, la app en UTC-6 y SQL Server en UTC).
        return JdbcSupport.insertar("guardar el código de recuperación",
                "INSERT INTO TokenRecuperacion (id_usuario, codigo_hash, fecha_expiracion) " +
                        "VALUES (?, ?, DATEADD(MINUTE, ?, SYSDATETIME()))",
                idUsuario, hash, minutosVigencia);
    }

    /** Marca el código como usado si coincide y no ha vencido. Devuelve true si lo consumió. */
    public boolean consumir(int idUsuario, byte[] hash) {
        return JdbcSupport.actualizar("consumir el código de recuperación", CONSUMIR, idUsuario, hash) > 0;
    }

    /** Igual que consumir(), pero dentro de la transacción que abre el Service (no cierra la conexión). */
    public boolean consumir(Connection connection, int idUsuario, byte[] hash) throws SQLException {
        return JdbcSupport.actualizar(connection, CONSUMIR, idUsuario, hash) > 0;
    }
}
