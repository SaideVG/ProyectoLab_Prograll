package universidad.sigelab.api.dto;

import java.time.LocalDateTime;

/** Un préstamo en el que participó un equipo. fechaDevolucion y condicion son null si aún no se devuelve. */
public record HistorialPrestamoDto(int idPrestamo, String solicitanteNombre, String solicitanteIdentificacion,
                                   LocalDateTime fechaPrestamo, LocalDateTime fechaEsperadaDevolucion,
                                   LocalDateTime fechaDevolucion, String condicion) { }
