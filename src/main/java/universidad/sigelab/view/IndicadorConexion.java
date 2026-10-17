package universidad.sigelab.view;

import universidad.sigelab.repository.RolRepository;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Punto de color + texto que indica si hay conexión con la base de datos.
 *
 * La comprobación se hace en segundo plano (SwingWorker) para no congelar la
 * interfaz mientras el servidor responde.
 */
class IndicadorConexion extends JPanel {

    private final Punto punto = new Punto();
    private final JLabel etiqueta = new JLabel();

    IndicadorConexion() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        etiqueta.setFont(Tema.pie());
        etiqueta.setForeground(Tema.TEXTO_SUAVE);
        punto.setAlignmentY(Component.CENTER_ALIGNMENT);
        etiqueta.setAlignmentY(Component.CENTER_ALIGNMENT);

        add(punto);
        add(Box.createHorizontalStrut(7));
        add(etiqueta);

        setEstado(false, "Comprobando conexión...");
    }

    void setEstado(boolean conectado, String texto) {
        punto.setColor(conectado ? Tema.EXITO : Tema.PELIGRO);
        etiqueta.setText(texto);
        revalidate();
        repaint();
    }

    /** Comprueba la base de datos sin bloquear la interfaz. */
    void verificar() {
        setEstado(false, "Comprobando conexión...");
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    new RolRepository().listar();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void done() {
                boolean conectado;
                try {
                    conectado = get();
                } catch (Exception e) {
                    conectado = false;
                }
                setEstado(conectado, conectado
                        ? "Conectado al servidor de BD"
                        : "Sin conexión al servidor de BD");
            }
        }.execute();
    }

    /** Circulito de color. */
    private static class Punto extends JComponent {

        private static final int DIAMETRO = 9;
        private Color color = Color.GRAY;

        Punto() {
            setPreferredSize(new Dimension(DIAMETRO, DIAMETRO));
            setMaximumSize(new Dimension(DIAMETRO, DIAMETRO));
            setMinimumSize(new Dimension(DIAMETRO, DIAMETRO));
        }

        void setColor(Color color) {
            this.color = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(0, (getHeight() - DIAMETRO) / 2, DIAMETRO, DIAMETRO);
            g2.dispose();
        }
    }
}
