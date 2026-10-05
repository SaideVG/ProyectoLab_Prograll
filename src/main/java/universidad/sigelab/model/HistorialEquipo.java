package universidad.sigelab.model;

import universidad.sigelab.enums.CondicionDevolucion;

import java.time.LocalDateTime;

/** Una fila del historial de préstamos de un equipo. Sin fecha de devolución, sigue prestado. */
public record HistorialEquipo(int idPrestamo, String solicitanteNombre, String solicitanteIdentificacion,
                              LocalDateTime fechaPrestamo, LocalDateTime fechaEsperadaDevolucion,
                              LocalDateTime fechaDevolucion, CondicionDevolucion condicion) { }
