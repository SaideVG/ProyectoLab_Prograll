package universidad.sigelab.repository;

import universidad.sigelab.enums.EstadoRegistro;
import universidad.sigelab.model.TipoEquipo;
import universidad.sigelab.util.JdbcSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TipoEquipoRepository {

    private static final String SELECT = "SELECT id_tipo_equipo, nombre, descripcion, estado FROM TipoEquipo ";

    public boolean guardar(TipoEquipo tipo) {
        String sql = "INSERT INTO TipoEquipo (nombre, descripcion, estado) VALUES (?, ?, ?)";
        tipo.setIdTipoEquipo(JdbcSupport.insertar("guardar el tipo de equipo", sql,
                tipo.getNombre(), tipo.getDescripcion(), tipo.getEstado()));
        return true;
    }

    public List<TipoEquipo> listar() {
        return JdbcSupport.consultar("consultar tipos de equipo", SELECT + "ORDER BY nombre", TipoEquipoRepository::mapear);
    }

    public List<TipoEquipo> listarActivos() {
        return JdbcSupport.consultar("consultar tipos de equipo",
                SELECT + "WHERE estado = 'ACTIVO' ORDER BY nombre", TipoEquipoRepository::mapear);
    }

    public Optional<TipoEquipo> buscarPorId(int id) {
        return JdbcSupport.buscar("buscar el tipo de equipo",
                SELECT + "WHERE id_tipo_equipo = ?", TipoEquipoRepository::mapear, id);
    }

    public Optional<TipoEquipo> buscarPorNombre(String nombre) {
        return JdbcSupport.buscar("buscar el tipo de equipo",
                SELECT + "WHERE nombre = ?", TipoEquipoRepository::mapear, nombre);
    }

    public boolean actualizar(TipoEquipo tipo) {
        String sql = "UPDATE TipoEquipo SET nombre = ?, descripcion = ?, estado = ? WHERE id_tipo_equipo = ?";
        return JdbcSupport.actualizar("actualizar el tipo de equipo", sql,
                tipo.getNombre(), tipo.getDescripcion(), tipo.getEstado(), tipo.getIdTipoEquipo()) > 0;
    }

    /** idExcluido permite ignorar el propio registro al actualizar; con 0 no se excluye ninguno. */
    public boolean existeNombre(String nombre, int idExcluido) {
        return JdbcSupport.existe("verificar el nombre del tipo de equipo",
                "SELECT 1 FROM TipoEquipo WHERE nombre = ? AND id_tipo_equipo <> ?", nombre, idExcluido);
    }

    private static TipoEquipo mapear(ResultSet resultSet) throws SQLException {
        return new TipoEquipo(resultSet.getInt("id_tipo_equipo"), resultSet.getString("nombre"),
                resultSet.getString("descripcion"), EstadoRegistro.valueOf(resultSet.getString("estado")));
    }
}
