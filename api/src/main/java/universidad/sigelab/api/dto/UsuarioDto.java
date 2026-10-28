package universidad.sigelab.api.dto;

/** Datos del usuario que la app puede mostrar. Nunca incluye el hash de la contraseña. */
public record UsuarioDto(int id, String usuario, String nombre, String apellido, String correo, String rol) { }
