package universidad.sigelab.model;

import universidad.sigelab.enums.EstadoMantenimiento;
import universidad.sigelab.enums.ResultadoMantenimiento;
import universidad.sigelab.enums.TipoMantenimiento;

import java.time.LocalDateTime;

public class Mantenimiento {
    private int idMantenimiento;
    private Equipo equipo;
    private LocalDateTime fechaIngreso;
    private TipoMantenimiento tipo;
    private String descripcion;
    private String responsable;
    private LocalDateTime fechaSalida;
    private ResultadoMantenimiento resultado;
    private EstadoMantenimiento estado;
    private String observaciones;
    private Usuario usuarioRegistra;

    public Mantenimiento() { }

    public Mantenimiento(Equipo equipo, LocalDateTime fechaIngreso, TipoMantenimiento tipo, String descripcion,
                         String responsable, Usuario usuarioRegistra) {
        this(0, equipo, fechaIngreso, tipo, descripcion, responsable, null, null,
                EstadoMantenimiento.EN_PROCESO, null, usuarioRegistra);
    }

    public Mantenimiento(int idMantenimiento, Equipo equipo, LocalDateTime fechaIngreso, TipoMantenimiento tipo,
                         String descripcion, String responsable, LocalDateTime fechaSalida,
                         ResultadoMantenimiento resultado, EstadoMantenimiento estado, String observaciones,
                         Usuario usuarioRegistra) {
        this.idMantenimiento = idMantenimiento;
        this.equipo = equipo;
        this.fechaIngreso = fechaIngreso;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.responsable = responsable;
        this.fechaSalida = fechaSalida;
        this.resultado = resultado;
        this.estado = estado;
        this.observaciones = observaciones;
        this.usuarioRegistra = usuarioRegistra;
    }

    public int getIdMantenimiento() { return idMantenimiento; }
    public void setIdMantenimiento(int idMantenimiento) { this.idMantenimiento = idMantenimiento; }
    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }
    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public TipoMantenimiento getTipo() { return tipo; }
    public void setTipo(TipoMantenimiento tipo) { this.tipo = tipo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public LocalDateTime getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDateTime fechaSalida) { this.fechaSalida = fechaSalida; }
    public ResultadoMantenimiento getResultado() { return resultado; }
    public void setResultado(ResultadoMantenimiento resultado) { this.resultado = resultado; }
    public EstadoMantenimiento getEstado() { return estado; }
    public void setEstado(EstadoMantenimiento estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Usuario getUsuarioRegistra() { return usuarioRegistra; }
    public void setUsuarioRegistra(Usuario usuarioRegistra) { this.usuarioRegistra = usuarioRegistra; }

    @Override
    public String toString() {
        return "Mantenimiento{" + "id=" + idMantenimiento +
                ", equipo=" + (equipo == null ? null : equipo.getCodigoInventario()) +
                ", tipo=" + tipo + ", estado=" + estado + ", resultado=" + resultado + '}';
    }
}
