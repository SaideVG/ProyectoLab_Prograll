package universidad.sigelab.api.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Mismos permisos por rol que la aplicación de escritorio.
 * Si cambian allá, deben cambiar aquí también.
 */
public enum RolUsuario {
    ADMIN(EnumSet.allOf(Modulo.class)),
    ENCARGADO(EnumSet.of(Modulo.EQUIPOS, Modulo.PRESTAMOS, Modulo.DEVOLUCIONES, Modulo.MANTENIMIENTO, Modulo.REPORTES)),
    DOCENTE(EnumSet.of(Modulo.REPORTES));

    private final Set<Modulo> modulos;

    RolUsuario(Set<Modulo> modulos) {
        this.modulos = modulos;
    }

    public boolean puedeAcceder(Modulo modulo) {
        return modulos.contains(modulo);
    }
}
