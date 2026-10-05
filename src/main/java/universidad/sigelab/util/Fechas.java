package universidad.sigelab.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/** Conversión y formato de fechas para las pantallas. */
public final class Fechas {
    public static final String PATRON_FECHA_HORA = "dd/MM/yyyy HH:mm";
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern(PATRON_FECHA_HORA);

    private Fechas() { }

    public static String texto(LocalDateTime fecha) {
        return fecha == null ? "" : fecha.format(FECHA_HORA);
    }

    /** Los JSpinner de fecha trabajan con java.util.Date; el modelo usa LocalDateTime. */
    public static LocalDateTime desdeDate(Date fecha) {
        return LocalDateTime.ofInstant(fecha.toInstant(), ZoneId.systemDefault());
    }

    public static Date aDate(LocalDateTime fecha) {
        return Date.from(fecha.atZone(ZoneId.systemDefault()).toInstant());
    }
}
