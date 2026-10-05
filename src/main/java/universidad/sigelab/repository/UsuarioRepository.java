package universidad.sigelab.repository;

import universidad.sigelab.enums.EstadoRegistro;
import universidad.sigelab.enums.RolUsuario;
import universidad.sigelab.model.Rol;
import universidad.sigelab.model.Usuario;
import universidad.sigelab.util.JdbcSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository {

    private static final String SELECT = """
            SELECT u.id_usuario, u.nombre, u.apellido, u.usuario, u.password_hash, u.correo,
                   u.estado, u.fecha_creacion,
                   r.id_rol, r.nombre AS rol_nombre, r.descripcion AS rol_descripcion
            FROM Usuario u
            INNER JOIN Rol r ON r.id_rol = u.id_rol
            """;

    public boolean guardar(Usuario usuario) {
        String sql = "INSERT INTO Usuario (nombre, apellido, usuario, password_hash, correo, id_rol, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        usuario.setIdUsuario(JdbcSupport.insertar("guardar el usuario", sql,
                usuario.getNombre(), usuario.getApellido(), usuario.getUsuario(), usuario.getPasswordHash(),
                usuario.getCorreo(), usuario.getRol().getIdRol(), usuario.getEstado()));
        return true;
    }

    public List<Usuario> listar() {
        return JdbcSupport.consultar("consultar usuarios", SELECT + "ORDER BY u.usuario", UsuarioRepository::mapear);
    }

    public Optional<Usuario> buscarPorId(int id) {
        return JdbcSupport.buscar("buscar el usuario", SELECT + "WHERE u.id_usuario = ?", UsuarioRepository::mapear, id);
    }

    public Optional<Usuario> buscarPorUsuario(String nombreUsuario) {
        return JdbcSupport.buscar("buscar el usuario", SELECT + "WHERE u.usuario = ?", UsuarioRepository::mapear, nombreUsuario);
    }

    public boolean actualizar(Usuario usuario) {
        String sql = "UPDATE Usuario SET nombre = ?, apellido = ?, usuario = ?, password_hash = ?, correo = ?, " +
                "id_rol = ?, estado = ? WHERE id_usuario = ?";
        return JdbcSupport.actualizar("actualizar el usuario", sql,
                usuario.getNombre(), usuario.getApellido(), usuario.getUsuario(), usuario.getPasswordHash(),
                usuario.getCorreo(), usuario.getRol().getIdRol(), usuario.getEstado(), usuario.getIdUsuario()) > 0;
    }

    /** idExcluido permite ignorar el propio registro al actualizar; con 0 no se excluye ninguno. */
    public boolean existeUsuario(String nombreUsuario, int idExcluido) {
        return JdbcSupport.existe("verificar el nombre de usuario",
                "SELECT 1 FROM Usuario WHERE usuario = ? AND id_usuario <> ?", nombreUsuario, idExcluido);
    }

    public boolean existeCorreo(String correo, int idExcluido) {
        return JdbcSupport.existe("verificar el correo",
                "SELECT 1 FROM Usuario WHERE correo = ? AND id_usuario <> ?", correo, idExcluido);
    }

    /**
     * Arma un Usuario con sus datos de identificación a partir de columnas con alias
     * (alias_id, alias_usuario, alias_nombre, alias_apellido). Lo usan otros repositorios
     * para saber quién registró un préstamo, un mantenimiento o un evento de auditoría.
     */
    static Usuario mapearBasico(ResultSet resultSet, String alias) throws SQLException {
        if (resultSet.getObject(alias + "_id") == null) return null;
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(resultSet.getInt(alias + "_id"));
        usuario.setUsuario(resultSet.getString(alias + "_usuario"));
        usuario.setNombre(resultSet.getString(alias + "_nombre"));
        usuario.setApellido(resultSet.getString(alias + "_apellido"));
        return usuario;
    }

    private static Usuario mapear(ResultSet resultSet) throws SQLException {
        Rol rol = new Rol(resultSet.getInt("id_rol"), RolUsuario.valueOf(resultSet.getString("rol_nombre")),
                resultSet.getString("rol_descripcion"));
        return new Usuario(resultSet.getInt("id_usuario"), resultSet.getString("nombre"),
                resultSet.getString("apellido"), resultSet.getString("usuario"),
                resultSet.getString("password_hash"), resultSet.getString("correo"), rol,
                EstadoRegistro.valueOf(resultSet.getString("estado")),
                JdbcSupport.localDateTime(resultSet, "fecha_creacion"));
    }
}
