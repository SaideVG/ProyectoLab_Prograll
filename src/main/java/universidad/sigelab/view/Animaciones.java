package universidad.sigelab.view;

import javax.swing.JFrame;
import javax.swing.Timer;
import java.awt.IllegalComponentStateException;

/** Animaciones de ventana compartidas. */
final class Animaciones {

    private Animaciones() { }

    /**
     * Muestra la ventana con un desvanecido de entrada.
     *
     * Si el sistema operativo no soporta transparentar ventanas, la ventana se
     * muestra normalmente y no se anima nada.
     */
    static void mostrarConDesvanecido(JFrame ventana) {
        try {
            ventana.setOpacity(0f);
        } catch (UnsupportedOperationException | IllegalComponentStateException e) {
            ventana.setVisible(true);
            return;
        }

        ventana.setVisible(true);

        Timer temporizador = new Timer(16, null);
        temporizador.addActionListener(e -> {
            float opacidad = Math.min(1f, ventana.getOpacity() + 0.07f);
            ventana.setOpacity(opacidad);
            if (opacidad >= 1f) {
                ((Timer) e.getSource()).stop();
            }
        });
        temporizador.start();
    }
}
