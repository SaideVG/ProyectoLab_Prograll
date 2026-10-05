package universidad.sigelab.repository;

import universidad.sigelab.enums.RolUsuario;
import universidad.sigelab.model.Rol;
import universidad.sigelab.util.JdbcSupport;

import java.util.List;
import java.util.Optional;

public class RolRepository {

    private static final String SELECT = "SELECT id_rol, nombre, descripcion FROM Rol ";

    public List<Rol> listar() {
        return JdbcSupport.consultar("consultar roles", SELECT + "ORDER BY id_rol", resultSet ->
                new Rol(resultSet.getInt("id_rol"), RolUsuario.valueOf(resultSet.getString("nombre")),
                        resultSet.getString("descripcion")));
    }

    public Optional<Rol> buscarPorId(int id) {
        return listar().stream().filter(rol -> rol.getIdRol() == id).findFirst();
    }
}
