package universidad.sigelab.repository;

import universidad.sigelab.enums.EstadoEquipo;
import universidad.sigelab.model.Equipo;
import universidad.sigelab.model.Laboratorio;
import universidad.sigelab.model.TipoEquipo;
import universidad.sigelab.util.JdbcSupport;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EquipoRepository {

    /*
     * Columnas y JOIN del equipo con su tipo y su laboratorio.
     * Los reutilizan los repositorios de préstamos y mantenimiento,
     * que también necesitan mostrar datos del equipo.
     */
    static final String COLUMNAS = """
            e.id_equipo, e.codigo_inventario, e.nombre AS equipo_nombre, e.marca, e.modelo, e.numero_serie,
            e.estado AS equipo_estado, e.observaciones AS equipo_observaciones,
            t.id_tipo_equipo, t.nombre AS tipo_nombre,
            l.id_laboratorio, l.codigo AS laboratorio_codigo, l.nombre AS laboratorio_nombre
            """;

    static final String JOINS = """
            INNER JOIN TipoEquipo t ON t.id_tipo_equipo = e.id_tipo_equipo
            INNER JOIN Laboratorio l ON l.id_laboratorio = e.id_laboratorio
            """;

    private static final String SELECT = "SELECT " + COLUMNAS + "FROM Equipo e " + JOINS;

    public boolean guardar(Equipo equipo) {
        String sql = "INSERT INTO Equipo (codigo_inventario, nombre, id_tipo_equipo, marca, modelo, numero_serie, " +
                "id_laboratorio, estado, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        equipo.setIdEquipo(JdbcSupport.insertar("guardar el equipo", sql,
                equipo.getCodigoInventario(), equipo.getNombre(), equipo.getTipo().getIdTipoEquipo(),
                equipo.getMarca(), equipo.getModelo(), equipo.getNumeroSerie(),
                equipo.getLaboratorio().getIdLaboratorio(), equipo.getEstado(), equipo.getObservaciones()));
        return true;
    }

    public List<Equipo> listar() {
        return JdbcSupport.consultar("consultar equipos", SELECT + "ORDER BY e.codigo_inventario", EquipoRepository::mapear);
    }

    public List<Equipo> listarPorEstado(EstadoEquipo estado) {
        return JdbcSupport.consultar("consultar equipos",
                SELECT + "WHERE e.estado = ? ORDER BY e.codigo_inventario", EquipoRepository::mapear, estado);
    }

    public Optional<Equipo> buscarPorId(int id) {
        return JdbcSupport.buscar("buscar el equipo", SELECT + "WHERE e.id_equipo = ?", EquipoRepository::mapear, id);
    }

    public Optional<Equipo> buscarPorCodigo(String codigo) {
        return JdbcSupport.buscar("buscar el equipo",
                SELECT + "WHERE e.codigo_inventario = ?", EquipoRepository::mapear, codigo);
    }

    /** Actualiza los datos descriptivos. El estado nunca se edita aquí: solo cambia con cambiarEstado(). */
    public boolean actualizar(Equipo equipo) {
        String sql = "UPDATE Equipo SET codigo_inventario = ?, nombre = ?, id_tipo_equipo = ?, marca = ?, modelo = ?, " +
                "numero_serie = ?, id_laboratorio = ?, observaciones = ? WHERE id_equipo = ?";
        return JdbcSupport.actualizar("actualizar el equipo", sql,
                equipo.getCodigoInventario(), equipo.getNombre(), equipo.getTipo().getIdTipoEquipo(),
                equipo.getMarca(), equipo.getModelo(), equipo.getNumeroSerie(),
                equipo.getLaboratorio().getIdLaboratorio(), equipo.getObservaciones(), equipo.getIdEquipo()) > 0;
    }

    /** idExcluido permite ignorar el propio registro al actualizar; con 0 no se excluye ninguno. */
    public boolean existeCodigo(String codigo, int idExcluido) {
        return JdbcSupport.existe("verificar el código del equipo",
                "SELECT 1 FROM Equipo WHERE codigo_inventario = ? AND id_equipo <> ?", codigo, idExcluido);
    }

    /**
     * Cambia el estado solo si el equipo todavía está en el estado esperado.
     * Devuelve false cuando no lo está: así dos operaciones simultáneas no pueden
     * prestar o devolver el mismo equipo. Siempre se usa dentro de una transacción.
     */
    public boolean cambiarEstado(Connection connection, int idEquipo, EstadoEquipo estadoEsperado,
                                 EstadoEquipo nuevoEstado) throws SQLException {
        return JdbcSupport.actualizar(connection,
                "UPDATE Equipo SET estado = ? WHERE id_equipo = ? AND estado = ?",
                nuevoEstado, idEquipo, estadoEsperado) == 1;
    }

    /** Cantidad de equipos por cada estado (dashboard). Un estado sin equipos no aparece en el mapa. */
    public Map<EstadoEquipo, Integer> contarPorEstado() {
        List<Map.Entry<EstadoEquipo, Integer>> filas = JdbcSupport.consultar("contar equipos por estado",
                "SELECT estado, COUNT(*) AS cantidad FROM Equipo GROUP BY estado",
                resultSet -> Map.entry(EstadoEquipo.valueOf(resultSet.getString("estado")), resultSet.getInt("cantidad")));
        Map<EstadoEquipo, Integer> cantidades = new EnumMap<>(EstadoEquipo.class);
        for (Map.Entry<EstadoEquipo, Integer> fila : filas) cantidades.put(fila.getKey(), fila.getValue());
        return cantidades;
    }

    static Equipo mapear(ResultSet resultSet) throws SQLException {
        TipoEquipo tipo = new TipoEquipo();
        tipo.setIdTipoEquipo(resultSet.getInt("id_tipo_equipo"));
        tipo.setNombre(resultSet.getString("tipo_nombre"));

        Laboratorio laboratorio = new Laboratorio();
        laboratorio.setIdLaboratorio(resultSet.getInt("id_laboratorio"));
        laboratorio.setCodigo(resultSet.getString("laboratorio_codigo"));
        laboratorio.setNombre(resultSet.getString("laboratorio_nombre"));

        return new Equipo(resultSet.getInt("id_equipo"), resultSet.getString("codigo_inventario"),
                resultSet.getString("equipo_nombre"), tipo, resultSet.getString("marca"),
                resultSet.getString("modelo"), resultSet.getString("numero_serie"), laboratorio,
                EstadoEquipo.valueOf(resultSet.getString("equipo_estado")),
                resultSet.getString("equipo_observaciones"));
    }
}
