package universidad.sigelab.model;

import universidad.sigelab.enums.EstadoRegistro;

import java.time.LocalDateTime;

public class Usuario {
    private int idUsuario;
    private String nombre;
    private String apellido;
    private String usuario;
    private String passwordHash;
    private String correo;
    private Rol rol;
    private EstadoRegistro estado;
    private LocalDateTime fechaCreacion;

    public Usuario() { }

    public Usuario(String nombre, String apellido, String usuario, String passwordHash, String correo, Rol rol) {
        this(0, nombre, apellido, usuario, passwordHash, correo, rol, EstadoRegistro.ACTIVO, null);
    }

    public Usuario(int idUsuario, String nombre, String apellido, String usuario, String passwordHash,
                   String correo, Rol rol, EstadoRegistro estado, LocalDateTime fechaCreacion) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.usuario = usuario;
        this.passwordHash = passwordHash;
        this.correo = correo;
        this.rol = rol;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public EstadoRegistro getEstado() { return estado; }
    public void setEstado(EstadoRegistro estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public boolean isActivo() { return estado == EstadoRegistro.ACTIVO; }
    public String getNombreCompleto() { return nombre + " " + apellido; }

    @Override
    public String toString() {
        return "Usuario{" + "id=" + idUsuario + ", usuario='" + usuario + '\'' +
                ", nombre='" + getNombreCompleto() + '\'' + ", rol=" + rol + ", estado=" + estado + '}';
    }
}
