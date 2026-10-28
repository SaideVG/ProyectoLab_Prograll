package universidad.sigelab.api.dto;

/** Equipo con el nombre de su tipo y de su laboratorio. estado es el valor de la base (DANADO sin ñ). */
public record EquipoDto(int id, String codigo, String nombre, String tipo, String marca, String modelo,
                        String numeroSerie, String laboratorioCodigo, String laboratorioNombre,
                        String estado, String observaciones) { }
