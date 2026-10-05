package universidad.sigelab.repository;

import universidad.sigelab.enums.EstadoRegistro;
import universidad.sigelab.model.Laboratorio;
import universidad.sigelab.util.JdbcSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class LaboratorioRepository {

    private static final String SELECT =
            "SELECT id_laboratorio, codigo, nombre, ubicacion, capacidad, estado FROM Laboratorio ";

    public boolean guardar(Laboratorio laboratorio) {
        String sql = "INSERT INTO Laboratorio (codigo, nombre, ubicacion, capacidad, estado) VALUES (?, ?, ?, ?, ?)";
        laboratorio.setIdLaboratorio(JdbcSupport.insertar("guardar el laboratorio", sql,
                laboratorio.getCodigo(), laboratorio.getNombre(), laboratorio.getUbicacion(),
                laboratorio.getCapacidad(), laboratorio.getEstado()));
        return true;
    }

    public List<Laboratorio> listar() {
        return JdbcSupport.consultar("consultar laboratorios", SELECT + "ORDER BY codigo", LaboratorioRepository::mapear);
    }

    public List<Laboratorio> listarActivos() {
        return JdbcSupport.consultar("consultar laboratorios",
                SELECT + "WHERE estado = 'ACTIVO' ORDER BY codigo", LaboratorioRepository::mapear);
    }

    public Optional<Laboratorio> buscarPorId(int id) {
        return JdbcSupport.buscar("buscar el laboratorio",
                SELECT + "WHERE id_laboratorio = ?", LaboratorioRepository::mapear, id);
    }

    public Optional<Laboratorio> buscarPorCodigo(String codigo) {
        return JdbcSupport.buscar("buscar el laboratorio",
                SELECT + "WHERE codigo = ?", LaboratorioRepository::mapear, codigo);
    }

    public boolean actualizar(Laboratorio laboratorio) {
        String sql = "UPDATE Laboratorio SET codigo = ?, nombre = ?, ubicacion = ?, capacidad = ?, estado = ? " +
                "WHERE id_laboratorio = ?";
        return JdbcSupport.actualizar("actualizar el laboratorio", sql,
                laboratorio.getCodigo(), laboratorio.getNombre(), laboratorio.getUbicacion(),
                laboratorio.getCapacidad(), laboratorio.getEstado(), laboratorio.getIdLaboratorio()) > 0;
    }

    /** idExcluido permite ignorar el propio registro al actualizar; con 0 no se excluye ninguno. */
    public boolean existeCodigo(String codigo, int idExcluido) {
        return JdbcSupport.existe("verificar el código del laboratorio",
                "SELECT 1 FROM Laboratorio WHERE codigo = ? AND id_laboratorio <> ?", codigo, idExcluido);
    }

    private static Laboratorio mapear(ResultSet resultSet) throws SQLException {
        return new Laboratorio(resultSet.getInt("id_laboratorio"), resultSet.getString("codigo"),
                resultSet.getString("nombre"), resultSet.getString("ubicacion"),
                resultSet.getInt("capacidad"), EstadoRegistro.valueOf(resultSet.getString("estado")));
    }
}
