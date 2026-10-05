package universidad.sigelab.model;

import universidad.sigelab.enums.EstadoRegistro;

public class Laboratorio {
    private int idLaboratorio;
    private String codigo;
    private String nombre;
    private String ubicacion;
    private int capacidad;
    private EstadoRegistro estado;

    public Laboratorio() { }

    public Laboratorio(String codigo, String nombre, String ubicacion, int capacidad) {
        this(0, codigo, nombre, ubicacion, capacidad, EstadoRegistro.ACTIVO);
    }

    public Laboratorio(int idLaboratorio, String codigo, String nombre, String ubicacion,
                       int capacidad, EstadoRegistro estado) {
        this.idLaboratorio = idLaboratorio;
        this.codigo = codigo;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    public int getIdLaboratorio() { return idLaboratorio; }
    public void setIdLaboratorio(int idLaboratorio) { this.idLaboratorio = idLaboratorio; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    public EstadoRegistro getEstado() { return estado; }
    public void setEstado(EstadoRegistro estado) { this.estado = estado; }

    /** Dos objetos son el mismo registro si tienen el mismo ID (lo usa JComboBox al seleccionar). */
    @Override
    public boolean equals(Object otro) {
        return otro instanceof Laboratorio o && o.idLaboratorio == idLaboratorio;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idLaboratorio);
    }

    /** Se muestra tal cual dentro de un JComboBox. */
    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
