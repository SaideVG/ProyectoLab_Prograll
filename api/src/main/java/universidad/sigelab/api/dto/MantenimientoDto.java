package universidad.sigelab.api.dto;

import java.time.LocalDateTime;

/** fechaSalida y resultado son null mientras el mantenimiento sigue EN_PROCESO. */
public record MantenimientoDto(int id, EquipoDto equipo, LocalDateTime fechaIngreso, String tipo,
                               String descripcion, String responsable, LocalDateTime fechaSalida,
                               String resultado, String estado, String observaciones, String registradoPor) { }
