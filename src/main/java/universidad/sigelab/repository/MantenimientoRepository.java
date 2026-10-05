package universidad.sigelab.repository;

import universidad.sigelab.enums.EstadoMantenimiento;
import universidad.sigelab.enums.ResultadoMantenimiento;
import universidad.sigelab.enums.TipoMantenimiento;
import universidad.sigelab.model.Mantenimiento;
import universidad.sigelab.util.JdbcSupport;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MantenimientoRepository {

    private static final String SELECT = "SELECT m.id_mantenimiento, m.fecha_ingreso, m.tipo, m.descripcion, " +
            "m.responsable, m.fecha_salida, m.resultado, m.estado AS mantenimiento_estado, " +
            "m.observaciones AS mantenimiento_observaciones, " +
            "u.id_usuario AS registra_id, u.usuario AS registra_usuario, " +
            "u.nombre AS registra_nombre, u.apellido AS registra_apellido, " + EquipoRepository.COLUMNAS +
            "FROM Mantenimiento m " +
            "INNER JOIN Equipo e ON e.id_equipo = m.id_equipo " + EquipoRepository.JOINS +
            "INNER JOIN Usuario u ON u.id_usuario = m.id_usuario_registra ";

    /*
     * ========================================================
     * ESCRITURA (siempre dentro de una transacción)
     * ========================================================
     */

    public void guardar(Connection connection, Mantenimiento mantenimiento) throws SQLException {
        String sql = "INSERT INTO Mantenimiento (id_equipo, fecha_ingreso, tipo, descripcion, responsable, estado, " +
                "id_usuario_registra) VALUES (?, ?, ?, ?, ?, ?, ?)";
        mantenimiento.setIdMantenimiento(JdbcSupport.insertar(connection, sql,
                mantenimiento.getEquipo().getIdEquipo(), mantenimiento.getFechaIngreso(), mantenimiento.getTipo(),
                mantenimiento.getDescripcion(), mantenimiento.getResponsable(), mantenimiento.getEstado(),
                mantenimiento.getUsuarioRegistra().getIdUsuario()));
    }

    /** Cierra el mantenimiento solo si sigue EN_PROCESO; devuelve false si alguien ya lo finalizó. */
    public boolean finalizar(Connection connection, Mantenimiento mantenimiento) throws SQLException {
        String sql = "UPDATE Mantenimiento SET fecha_salida = ?, resultado = ?, estado = ?, observaciones = ? " +
                "WHERE id_mantenimiento = ? AND estado = 'EN_PROCESO'";
        return JdbcSupport.actualizar(connection, sql,
                mantenimiento.getFechaSalida(), mantenimiento.getResultado(), mantenimiento.getEstado(),
                mantenimiento.getObservaciones(), mantenimiento.getIdMantenimiento()) == 1;
    }

    /*
     * ========================================================
     * CONSULTAS
     * ========================================================
     */

    public Optional<Mantenimiento> buscarPorId(int id) {
        return JdbcSupport.buscar("buscar el mantenimiento",
                SELECT + "WHERE m.id_mantenimiento = ?", MantenimientoRepository::mapear, id);
    }

    public List<Mantenimiento> listar() {
        return JdbcSupport.consultar("consultar mantenimientos",
                SELECT + "ORDER BY m.fecha_ingreso DESC, m.id_mantenimiento DESC", MantenimientoRepository::mapear);
    }

    public List<Mantenimiento> listarEnProceso() {
        return JdbcSupport.consultar("consultar mantenimientos",
                SELECT + "WHERE m.estado = 'EN_PROCESO' ORDER BY m.fecha_ingreso DESC, m.id_mantenimiento DESC",
                MantenimientoRepository::mapear);
    }

    /** Historial de mantenimiento de un equipo, del más reciente al más antiguo. */
    public List<Mantenimiento> listarPorEquipo(int idEquipo) {
        return JdbcSupport.consultar("consultar el historial de mantenimiento",
                SELECT + "WHERE m.id_equipo = ? ORDER BY m.fecha_ingreso DESC, m.id_mantenimiento DESC",
                MantenimientoRepository::mapear, idEquipo);
    }

    private static Mantenimiento mapear(ResultSet resultSet) throws SQLException {
        String resultado = resultSet.getString("resultado");
        return new Mantenimiento(resultSet.getInt("id_mantenimiento"), EquipoRepository.mapear(resultSet),
                JdbcSupport.localDateTime(resultSet, "fecha_ingreso"),
                TipoMantenimiento.valueOf(resultSet.getString("tipo")),
                resultSet.getString("descripcion"), resultSet.getString("responsable"),
                JdbcSupport.localDateTime(resultSet, "fecha_salida"),
                resultado == null ? null : ResultadoMantenimiento.valueOf(resultado),
                EstadoMantenimiento.valueOf(resultSet.getString("mantenimiento_estado")),
                resultSet.getString("mantenimiento_observaciones"),
                UsuarioRepository.mapearBasico(resultSet, "registra"));
    }
}
