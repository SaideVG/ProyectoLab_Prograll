package universidad.sigelab.api.dto;

import java.time.LocalDateTime;

/**
 * Encabezado del préstamo con sus totales.
 * vencido no se guarda en la base: se calcula igual que en el escritorio (RN-17).
 */
public record PrestamoDto(int id, String solicitanteNombre, String solicitanteIdentificacion,
                          LocalDateTime fechaPrestamo, LocalDateTime fechaEsperadaDevolucion,
                          String estado, String observaciones, String registradoPor,
                          int totalEquipos, int pendientes, boolean vencido) { }
