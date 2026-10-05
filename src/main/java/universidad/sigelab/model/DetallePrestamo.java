package universidad.sigelab.model;

/** Relaciona un equipo con un préstamo. Sin devolución registrada, el equipo sigue pendiente. */
public class DetallePrestamo {
    private int idDetallePrestamo;
    private int idPrestamo;
    private Equipo equipo;
    private Devolucion devolucion;

    public DetallePrestamo() { }

    public DetallePrestamo(Equipo equipo) {
        this(0, 0, equipo, null);
    }

    public DetallePrestamo(int idDetallePrestamo, int idPrestamo, Equipo equipo, Devolucion devolucion) {
        this.idDetallePrestamo = idDetallePrestamo;
        this.idPrestamo = idPrestamo;
        this.equipo = equipo;
        this.devolucion = devolucion;
    }

    public int getIdDetallePrestamo() { return idDetallePrestamo; }
    public void setIdDetallePrestamo(int idDetallePrestamo) { this.idDetallePrestamo = idDetallePrestamo; }
    public int getIdPrestamo() { return idPrestamo; }
    public void setIdPrestamo(int idPrestamo) { this.idPrestamo = idPrestamo; }
    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }
    public Devolucion getDevolucion() { return devolucion; }
    public void setDevolucion(Devolucion devolucion) { this.devolucion = devolucion; }

    public boolean isPendiente() { return devolucion == null; }

    @Override
    public String toString() {
        return "DetallePrestamo{" + "id=" + idDetallePrestamo + ", prestamo=" + idPrestamo +
                ", equipo=" + (equipo == null ? null : equipo.getCodigoInventario()) +
                ", pendiente=" + isPendiente() + '}';
    }
}
