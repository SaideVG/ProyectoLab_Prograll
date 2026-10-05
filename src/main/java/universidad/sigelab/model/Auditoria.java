package universidad.sigelab.model;

import universidad.sigelab.enums.AccionAuditoria;

import java.time.LocalDateTime;

public class Auditoria {
    private long idAuditoria;
    /** Puede ser null: un login fallido con un usuario que no existe no tiene a quién asociarse. */
    private Usuario usuario;
    private LocalDateTime fechaHora;
    private AccionAuditoria accion;
    private String entidad;
    private Integer idRegistro;
    private String descripcion;

    public Auditoria() { }

    public Auditoria(Usuario usuario, AccionAuditoria accion, String entidad, Integer idRegistro, String descripcion) {
        this(0, usuario, LocalDateTime.now(), accion, entidad, idRegistro, descripcion);
    }

    public Auditoria(long idAuditoria, Usuario usuario, LocalDateTime fechaHora, AccionAuditoria accion,
                     String entidad, Integer idRegistro, String descripcion) {
        this.idAuditoria = idAuditoria;
        this.usuario = usuario;
        this.fechaHora = fechaHora;
        this.accion = accion;
        this.entidad = entidad;
        this.idRegistro = idRegistro;
        this.descripcion = descripcion;
    }

    public long getIdAuditoria() { return idAuditoria; }
    public void setIdAuditoria(long idAuditoria) { this.idAuditoria = idAuditoria; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public AccionAuditoria getAccion() { return accion; }
    public void setAccion(AccionAuditoria accion) { this.accion = accion; }
    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }
    public Integer getIdRegistro() { return idRegistro; }
    public void setIdRegistro(Integer idRegistro) { this.idRegistro = idRegistro; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() {
        return "Auditoria{" + "id=" + idAuditoria +
                ", usuario=" + (usuario == null ? null : usuario.getUsuario()) +
                ", fechaHora=" + fechaHora + ", accion=" + accion + ", entidad='" + entidad + '\'' +
                ", idRegistro=" + idRegistro + '}';
    }
}
