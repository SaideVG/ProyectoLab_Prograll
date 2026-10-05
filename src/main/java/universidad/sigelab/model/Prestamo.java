package universidad.sigelab.model;

import universidad.sigelab.enums.EstadoPrestamo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Encabezado del préstamo; los equipos entregados viven en DetallePrestamo. */
public class Prestamo {
    private int idPrestamo;
    private String solicitanteNombre;
    private String solicitanteIdentificacion;
    private LocalDateTime fechaPrestamo;
    private LocalDateTime fechaEsperadaDevolucion;
    private Usuario usuarioRegistra;
    private EstadoPrestamo estado;
    private String observaciones;
    private List<DetallePrestamo> detalles = new ArrayList<>();

    public Prestamo() { }

    public Prestamo(String solicitanteNombre, String solicitanteIdentificacion, LocalDateTime fechaPrestamo,
                    LocalDateTime fechaEsperadaDevolucion, Usuario usuarioRegistra, String observaciones) {
        this(0, solicitanteNombre, solicitanteIdentificacion, fechaPrestamo, fechaEsperadaDevolucion,
                usuarioRegistra, EstadoPrestamo.ACTIVO, observaciones);
    }

    public Prestamo(int idPrestamo, String solicitanteNombre, String solicitanteIdentificacion,
                    LocalDateTime fechaPrestamo, LocalDateTime fechaEsperadaDevolucion,
                    Usuario usuarioRegistra, EstadoPrestamo estado, String observaciones) {
        this.idPrestamo = idPrestamo;
        this.solicitanteNombre = solicitanteNombre;
        this.solicitanteIdentificacion = solicitanteIdentificacion;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaEsperadaDevolucion = fechaEsperadaDevolucion;
        this.usuarioRegistra = usuarioRegistra;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    public int getIdPrestamo() { return idPrestamo; }
    public void setIdPrestamo(int idPrestamo) { this.idPrestamo = idPrestamo; }
    public String getSolicitanteNombre() { return solicitanteNombre; }
    public void setSolicitanteNombre(String solicitanteNombre) { this.solicitanteNombre = solicitanteNombre; }
    public String getSolicitanteIdentificacion() { return solicitanteIdentificacion; }
    public void setSolicitanteIdentificacion(String solicitanteIdentificacion) { this.solicitanteIdentificacion = solicitanteIdentificacion; }
    public LocalDateTime getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDateTime fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }
    public LocalDateTime getFechaEsperadaDevolucion() { return fechaEsperadaDevolucion; }
    public void setFechaEsperadaDevolucion(LocalDateTime fechaEsperadaDevolucion) { this.fechaEsperadaDevolucion = fechaEsperadaDevolucion; }
    public Usuario getUsuarioRegistra() { return usuarioRegistra; }
    public void setUsuarioRegistra(Usuario usuarioRegistra) { this.usuarioRegistra = usuarioRegistra; }
    public EstadoPrestamo getEstado() { return estado; }
    public void setEstado(EstadoPrestamo estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public List<DetallePrestamo> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePrestamo> detalles) { this.detalles = detalles; }

    @Override
    public String toString() {
        return "Prestamo{" + "id=" + idPrestamo + ", solicitante='" + solicitanteNombre + '\'' +
                ", fechaPrestamo=" + fechaPrestamo + ", fechaEsperadaDevolucion=" + fechaEsperadaDevolucion +
                ", estado=" + estado + ", equipos=" + detalles.size() + '}';
    }
}
