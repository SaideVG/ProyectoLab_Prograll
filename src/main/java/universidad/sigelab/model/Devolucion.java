package universidad.sigelab.model;

import universidad.sigelab.enums.CondicionDevolucion;

import java.time.LocalDateTime;

public class Devolucion {
    private int idDevolucion;
    private int idDetallePrestamo;
    private LocalDateTime fechaDevolucion;
    private CondicionDevolucion condicion;
    private String observacion;
    private Usuario usuarioRecibe;

    public Devolucion() { }

    public Devolucion(int idDetallePrestamo, LocalDateTime fechaDevolucion, CondicionDevolucion condicion,
                      String observacion, Usuario usuarioRecibe) {
        this(0, idDetallePrestamo, fechaDevolucion, condicion, observacion, usuarioRecibe);
    }

    public Devolucion(int idDevolucion, int idDetallePrestamo, LocalDateTime fechaDevolucion,
                      CondicionDevolucion condicion, String observacion, Usuario usuarioRecibe) {
        this.idDevolucion = idDevolucion;
        this.idDetallePrestamo = idDetallePrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.condicion = condicion;
        this.observacion = observacion;
        this.usuarioRecibe = usuarioRecibe;
    }

    public int getIdDevolucion() { return idDevolucion; }
    public void setIdDevolucion(int idDevolucion) { this.idDevolucion = idDevolucion; }
    public int getIdDetallePrestamo() { return idDetallePrestamo; }
    public void setIdDetallePrestamo(int idDetallePrestamo) { this.idDetallePrestamo = idDetallePrestamo; }
    public LocalDateTime getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDateTime fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }
    public CondicionDevolucion getCondicion() { return condicion; }
    public void setCondicion(CondicionDevolucion condicion) { this.condicion = condicion; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public Usuario getUsuarioRecibe() { return usuarioRecibe; }
    public void setUsuarioRecibe(Usuario usuarioRecibe) { this.usuarioRecibe = usuarioRecibe; }

    @Override
    public String toString() {
        return "Devolucion{" + "id=" + idDevolucion + ", detalle=" + idDetallePrestamo +
                ", fecha=" + fechaDevolucion + ", condicion=" + condicion + '}';
    }
}
