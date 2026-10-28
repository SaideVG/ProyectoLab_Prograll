package universidad.sigelab.api.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import universidad.sigelab.api.enums.RolUsuario;
import universidad.sigelab.api.model.Usuario;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class UsuarioRepository {

    private static final String SELECT = """
            SELECT u.id_usuario, u.usuario, u.nombre, u.apellido, u.correo, u.password_hash, u.estado,
                   r.nombre AS rol
            FROM Usuario u
            INNER JOIN Rol r ON r.id_rol = u.id_rol
            """;

    private final JdbcTemplate jdbc;

    public UsuarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Usuario> buscarPorUsuario(String nombreUsuario) {
        return jdbc.query(SELECT + "WHERE u.usuario = ?", UsuarioRepository::mapear, nombreUsuario)
                .stream().findFirst();
    }

    public Optional<Usuario> buscarPorId(int id) {
        return jdbc.query(SELECT + "WHERE u.id_usuario = ?", UsuarioRepository::mapear, id)
                .stream().findFirst();
    }

    private static Usuario mapear(ResultSet resultSet, int fila) throws SQLException {
        return new Usuario(resultSet.getInt("id_usuario"), resultSet.getString("usuario"),
                resultSet.getString("nombre"), resultSet.getString("apellido"), resultSet.getString("correo"),
                resultSet.getString("password_hash"), RolUsuario.valueOf(resultSet.getString("rol")),
                "ACTIVO".equals(resultSet.getString("estado")));
    }
}
