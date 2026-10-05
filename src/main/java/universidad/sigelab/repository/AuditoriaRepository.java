package universidad.sigelab.repository;

import universidad.sigelab.enums.AccionAuditoria;
import universidad.sigelab.model.Auditoria;
import universidad.sigelab.util.JdbcSupport;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AuditoriaRepository {

    private static final String INSERT =
            "INSERT INTO Auditoria (id_usuario, fecha_hora, accion, entidad, id_registro, descripcion) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    /* TOP 500: la pantalla muestra los eventos más recientes, no todo el historial. */
    private static final String SELECT = """
            SELECT TOP 500 a.id_auditoria, a.fecha_hora, a.accion, a.entidad, a.id_registro, a.descripcion,
                   u.id_usuario AS autor_id, u.usuario AS autor_usuario,
                   u.nombre AS autor_nombre, u.apellido AS autor_apellido
            FROM Auditoria a
            LEFT JOIN Usuario u ON u.id_usuario = a.id_usuario
            """;

    private static final String ORDEN = "ORDER BY a.fecha_hora DESC, a.id_auditoria DESC";

    /** Registra un evento suelto (por ejemplo, un intento de login). */
    public void registrar(Auditoria auditoria) {
        JdbcSupport.actualizar("registrar la auditoría", INSERT, parametros(auditoria));
    }

    /** Registra el evento dentro de la misma transacción de la operación que lo origina. */
    public void registrar(Connection connection, Auditoria auditoria) throws SQLException {
        JdbcSupport.actualizar(connection, INSERT, parametros(auditoria));
    }

    public List<Auditoria> listar() {
        return JdbcSupport.consultar("consultar la auditoría", SELECT + ORDEN, AuditoriaRepository::mapear);
    }

    public List<Auditoria> listarPorAccion(AccionAuditoria accion) {
        return JdbcSupport.consultar("consultar la auditoría",
                SELECT + "WHERE a.accion = ? " + ORDEN, AuditoriaRepository::mapear, accion);
    }

    private static Object[] parametros(Auditoria auditoria) {
        Integer idUsuario = auditoria.getUsuario() == null ? null : auditoria.getUsuario().getIdUsuario();
        return new Object[]{idUsuario, auditoria.getFechaHora(), auditoria.getAccion(), auditoria.getEntidad(),
                auditoria.getIdRegistro(), auditoria.getDescripcion()};
    }

    private static Auditoria mapear(ResultSet resultSet) throws SQLException {
        Integer idRegistro = resultSet.getObject("id_registro") == null ? null : resultSet.getInt("id_registro");
        return new Auditoria(resultSet.getLong("id_auditoria"), UsuarioRepository.mapearBasico(resultSet, "autor"),
                JdbcSupport.localDateTime(resultSet, "fecha_hora"),
                AccionAuditoria.valueOf(resultSet.getString("accion")), resultSet.getString("entidad"),
                idRegistro, resultSet.getString("descripcion"));
    }
}
