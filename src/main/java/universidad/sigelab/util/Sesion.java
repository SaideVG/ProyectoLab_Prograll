package universidad.sigelab.util;

import universidad.sigelab.enums.Modulo;
import universidad.sigelab.model.Usuario;

/** Usuario que inició sesión. Los Services lo usan para validar permisos y saber quién opera. */
public final class Sesion {
    private static Usuario usuarioActual;

    private Sesion() { }

    public static void iniciar(Usuario usuario) { usuarioActual = usuario; }
    public static void cerrar() { usuarioActual = null; }
    public static Usuario getUsuarioActual() { return usuarioActual; }

    /** Ocultar un botón del menú no basta: cada Service debe exigir el acceso antes de operar. */
    public static void exigirAcceso(Modulo modulo) {
        if (usuarioActual == null) throw new BusinessException("Debe iniciar sesión.");
        if (!usuarioActual.getRol().getNombre().puedeAcceder(modulo)) {
            throw new BusinessException("Su rol no tiene permiso para esta operación.");
        }
    }
}
