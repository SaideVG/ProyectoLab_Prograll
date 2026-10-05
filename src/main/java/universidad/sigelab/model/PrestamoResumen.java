package universidad.sigelab.model;

import java.time.LocalDateTime;

/** Un préstamo junto con cuántos equipos incluye y cuántos faltan por devolver. */
public record PrestamoResumen(Prestamo prestamo, int totalEquipos, int pendientes) {

    /** RN-17: está vencido si la fecha esperada ya pasó y todavía tiene equipos pendientes. */
    public boolean vencido(LocalDateTime ahora) {
        return pendientes > 0 && prestamo.getFechaEsperadaDevolucion().isBefore(ahora);
    }
}
