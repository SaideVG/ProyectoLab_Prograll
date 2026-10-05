package universidad.sigelab.view;

import universidad.sigelab.util.BusinessException;
import universidad.sigelab.util.Fechas;

import javax.swing.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;

/** Convierte un JSpinner en un selector de fecha y hora con el formato dd/MM/yyyy HH:mm. */
final class SelectorFecha {
    private SelectorFecha() { }

    static void configurar(JSpinner spinner, LocalDateTime valorInicial) {
        spinner.setModel(new SpinnerDateModel());
        spinner.setEditor(new JSpinner.DateEditor(spinner, Fechas.PATRON_FECHA_HORA));
        spinner.setValue(Fechas.aDate(valorInicial));
    }

    /** Toma también lo que el usuario escribió a mano y todavía no confirmó con Enter. */
    static LocalDateTime leer(JSpinner spinner, String campo) {
        try {
            spinner.commitEdit();
        } catch (ParseException e) {
            throw new BusinessException("La fecha de " + campo + " no es válida. Use el formato " +
                    Fechas.PATRON_FECHA_HORA + ".");
        }
        return Fechas.desdeDate((Date) spinner.getValue());
    }
}
