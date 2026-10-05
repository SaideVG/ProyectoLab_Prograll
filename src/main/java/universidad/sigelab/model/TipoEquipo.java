package universidad.sigelab.model;

import universidad.sigelab.enums.EstadoRegistro;

public class TipoEquipo {
    private int idTipoEquipo;
    private String nombre;
    private String descripcion;
    private EstadoRegistro estado;

    public TipoEquipo() { }

    public TipoEquipo(String nombre, String descripcion) {
        this(0, nombre, descripcion, EstadoRegistro.ACTIVO);
    }

    public TipoEquipo(int idTipoEquipo, String nombre, String descripcion, EstadoRegistro estado) {
        this.idTipoEquipo = idTipoEquipo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public int getIdTipoEquipo() { return idTipoEquipo; }
    public void setIdTipoEquipo(int idTipoEquipo) { this.idTipoEquipo = idTipoEquipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public EstadoRegistro getEstado() { return estado; }
    public void setEstado(EstadoRegistro estado) { this.estado = estado; }

    /** Dos objetos son el mismo registro si tienen el mismo ID (lo usa JComboBox al seleccionar). */
    @Override
    public boolean equals(Object otro) {
        return otro instanceof TipoEquipo o && o.idTipoEquipo == idTipoEquipo;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idTipoEquipo);
    }

    /** Se muestra tal cual dentro de un JComboBox. */
    @Override
    public String toString() {
        return nombre;
    }
}
