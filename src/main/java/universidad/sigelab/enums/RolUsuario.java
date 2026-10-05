package universidad.sigelab.enums;

import java.util.EnumSet;
import java.util.Set;

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
