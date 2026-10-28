package universidad.sigelab.api.dto;

import java.time.LocalDateTime;

/** Un equipo dentro de un préstamo y, si ya se devolvió, cómo regresó. */
public record DetallePrestamoDto(int idDetalle, EquipoDto equipo, boolean devuelto,
                                 LocalDateTime fechaDevolucion, String condicion, String observacion) { }
