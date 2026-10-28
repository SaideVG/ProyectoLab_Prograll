package universidad.sigelab.api.model;

import universidad.sigelab.api.dto.UsuarioDto;
import universidad.sigelab.api.enums.RolUsuario;

/**
 * Usuario tal como está en la tabla Usuario.
 * Se queda dentro de la API: a la app solo sale como UsuarioDto, sin el hash.
 */
public record Usuario(int id, String usuario, String nombre, String apellido, String correo,
                      String passwordHash, RolUsuario rol, boolean activo) {

    public UsuarioDto aDto() {
        return new UsuarioDto(id, usuario, nombre, apellido, correo, rol.name());
    }
}
