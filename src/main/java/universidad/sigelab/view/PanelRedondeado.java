package universidad.sigelab.view;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Panel blanco con esquinas redondeadas; se usa como tarjeta contenedora. */
class PanelRedondeado extends JPanel {

    private int radio = 24;
    private Color colorFondo = Tema.SUPERFICIE;
    private Color colorBorde = Tema.BORDE;

    PanelRedondeado() {
        setOpaque(false);
    }

    void setRadio(int radio) {
        this.radio = radio;
        repaint();
    }

    void setColorFondo(Color colorFondo) {
        this.colorFondo = colorFondo;
        repaint();
    }

    void setColorBorde(Color colorBorde) {
        this.colorBorde = colorBorde;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ancho = getWidth() - 1;
        int alto = getHeight() - 1;

        g2.setColor(colorFondo);
        g2.fillRoundRect(0, 0, ancho, alto, radio, radio);

        if (colorBorde != null) {
            g2.setColor(colorBorde);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, ancho, alto, radio, radio);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
