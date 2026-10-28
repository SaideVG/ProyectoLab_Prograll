package universidad.sigelab.api.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import universidad.sigelab.api.dto.MantenimientoDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class MantenimientoRepository {

    private static final String SELECT = "SELECT m.id_mantenimiento, m.fecha_ingreso, m.tipo, m.descripcion, " +
            "m.responsable, m.fecha_salida, m.resultado, m.estado AS mantenimiento_estado, " +
            "m.observaciones AS mantenimiento_observaciones, " +
            "u.nombre AS registra_nombre, u.apellido AS registra_apellido, " + EquipoRepository.COLUMNAS +
            "FROM Mantenimiento m " +
            "INNER JOIN Equipo e ON e.id_equipo = m.id_equipo " + EquipoRepository.JOINS +
            "INNER JOIN Usuario u ON u.id_usuario = m.id_usuario_registra ";

    private static final String ORDEN = "ORDER BY m.fecha_ingreso DESC, m.id_mantenimiento DESC";

    private final JdbcTemplate jdbc;

    public MantenimientoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<MantenimientoDto> listar() {
        return jdbc.query(SELECT + ORDEN, MantenimientoRepository::mapear);
    }

    public List<MantenimientoDto> listarEnProceso() {
        return jdbc.query(SELECT + "WHERE m.estado = 'EN_PROCESO' " + ORDEN, MantenimientoRepository::mapear);
    }

    public List<MantenimientoDto> listarPorEquipo(int idEquipo) {
        return jdbc.query(SELECT + "WHERE m.id_equipo = ? " + ORDEN, MantenimientoRepository::mapear, idEquipo);
    }

    private static MantenimientoDto mapear(ResultSet resultSet, int fila) throws SQLException {
        return new MantenimientoDto(resultSet.getInt("id_mantenimiento"), EquipoRepository.mapear(resultSet, fila),
                PrestamoRepository.fecha(resultSet, "fecha_ingreso"), resultSet.getString("tipo"),
                resultSet.getString("descripcion"), resultSet.getString("responsable"),
                PrestamoRepository.fecha(resultSet, "fecha_salida"), resultSet.getString("resultado"),
                resultSet.getString("mantenimiento_estado"), resultSet.getString("mantenimiento_observaciones"),
                resultSet.getString("registra_nombre") + " " + resultSet.getString("registra_apellido"));
    }
}
