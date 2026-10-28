package universidad.sigelab.api.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import universidad.sigelab.api.dto.EquipoDto;
import universidad.sigelab.api.enums.EstadoEquipo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class EquipoRepository {

    /*
     * Mismas columnas y JOIN que EquipoRepository del escritorio.
     * Los reutilizan los repositorios de préstamos y mantenimiento.
     */
    static final String COLUMNAS = """
            e.id_equipo, e.codigo_inventario, e.nombre AS equipo_nombre, e.marca, e.modelo, e.numero_serie,
            e.estado AS equipo_estado, e.observaciones AS equipo_observaciones,
            t.nombre AS tipo_nombre, l.codigo AS laboratorio_codigo, l.nombre AS laboratorio_nombre
            """;

    static final String JOINS = """
            INNER JOIN TipoEquipo t ON t.id_tipo_equipo = e.id_tipo_equipo
            INNER JOIN Laboratorio l ON l.id_laboratorio = e.id_laboratorio
            """;

    private static final String SELECT = "SELECT " + COLUMNAS + "FROM Equipo e " + JOINS;

    private final JdbcTemplate jdbc;

    public EquipoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Lista los equipos filtrando por estado y por texto (código o nombre).
     * Cualquiera de los dos filtros puede venir en null.
     */
    public List<EquipoDto> buscar(EstadoEquipo estado, String texto) {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (estado != null) {
            sql.append("AND e.estado = ? ");
            parametros.add(estado.name());
        }
        if (texto != null) {
            String patron = "%" + escaparLike(texto) + "%";
            sql.append("AND (e.codigo_inventario LIKE ? OR e.nombre LIKE ?) ");
            parametros.add(patron);
            parametros.add(patron);
        }
        sql.append("ORDER BY e.codigo_inventario");

        return jdbc.query(sql.toString(), EquipoRepository::mapear, parametros.toArray());
    }

    public Optional<EquipoDto> buscarPorCodigo(String codigo) {
        return jdbc.query(SELECT + "WHERE e.codigo_inventario = ?", EquipoRepository::mapear, codigo)
                .stream().findFirst();
    }

    public Map<EstadoEquipo, Integer> contarPorEstado() {
        Map<EstadoEquipo, Integer> cantidades = new EnumMap<>(EstadoEquipo.class);
        jdbc.query("SELECT estado, COUNT(*) AS cantidad FROM Equipo GROUP BY estado", resultSet -> {
            cantidades.put(EstadoEquipo.valueOf(resultSet.getString("estado")), resultSet.getInt("cantidad"));
        });
        return cantidades;
    }

    /** En SQL Server % _ y [ son comodines de LIKE; entre corchetes se buscan como texto normal. */
    private static String escaparLike(String texto) {
        return texto.replace("[", "[[]").replace("%", "[%]").replace("_", "[_]");
    }

    static EquipoDto mapear(ResultSet resultSet, int fila) throws SQLException {
        return new EquipoDto(resultSet.getInt("id_equipo"), resultSet.getString("codigo_inventario"),
                resultSet.getString("equipo_nombre"), resultSet.getString("tipo_nombre"),
                resultSet.getString("marca"), resultSet.getString("modelo"), resultSet.getString("numero_serie"),
                resultSet.getString("laboratorio_codigo"), resultSet.getString("laboratorio_nombre"),
                resultSet.getString("equipo_estado"), resultSet.getString("equipo_observaciones"));
    }
}
