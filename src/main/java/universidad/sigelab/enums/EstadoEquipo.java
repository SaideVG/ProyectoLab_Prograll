package universidad.sigelab.enums;

public enum EstadoEquipo {
    DISPONIBLE, PRESTADO, DANADO, MANTENIMIENTO, BAJA;

    /** Texto para las pantallas. En la base de datos siempre se guarda name(). */
    @Override
    public String toString() {
        return this == DANADO ? "DAÑADO" : name();
    }
}
