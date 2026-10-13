package universidad.sigelab.view;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Panel decorativo que acompaña al formulario de login.
 *
 * Pinta un degradado azul muy claro con formas suaves y un matraz (frasco de
 * laboratorio) dentro de una tarjeta blanca, para dar identidad visual a la
 * pantalla sin depender de imágenes externas.
 */
class PanelIlustracion extends JPanel {

    PanelIlustracion() {
        setOpaque(true);
        setBackground(new Color(0xF4F8FD));
        setPreferredSize(new Dimension(520, 620));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Degradado vertical muy suave.
        g2.setPaint(new GradientPaint(0, 0, new Color(0xF8FBFF), 0, h, new Color(0xE3ECFB)));
        g2.fillRect(0, 0, w, h);

        // Círculos decorativos translúcidos.
        traslucido(g2, new Color(0x1D5BD8), 38);
        g2.fill(new Ellipse2D.Double(w * 0.62, -h * 0.10, w * 0.55, w * 0.55));
        traslucido(g2, new Color(0x60A5FA), 50);
        g2.fill(new Ellipse2D.Double(w * 0.74, h * 0.60, w * 0.30, w * 0.30));
        traslucido(g2, Color.WHITE, 150);
        g2.fill(new Ellipse2D.Double(-w * 0.18, h * 0.52, w * 0.62, w * 0.62));
        traslucido(g2, Color.WHITE, 120);
        g2.fill(new Ellipse2D.Double(w * 0.06, h * 0.08, w * 0.24, w * 0.24));

        // Emblema central: tarjeta blanca con el matraz.
        double lado = Math.min(w, h) * 0.44;
        double ex = (w - lado) / 2.0;
        double ey = (h - lado) / 2.0;

        g2.setColor(new Color(255, 255, 255, 240));
        g2.fill(new RoundRectangle2D.Double(ex, ey, lado, lado, lado * 0.30, lado * 0.30));
        g2.setColor(new Color(0xD9E4F6));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Double(ex, ey, lado, lado, lado * 0.30, lado * 0.30));

        dibujarMatraz(g2, w / 2.0, ey + lado / 2.0, lado / 250.0);

        g2.dispose();
    }

    private void traslucido(Graphics2D g2, Color base, int alpha) {
        g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha));
    }

    /** Dibuja un matraz de laboratorio centrado en (cx, cy). */
    private void dibujarMatraz(Graphics2D g2, double cx, double cy, double e) {
        double anchoCuello = 30 * e;
        double altoCuello = 54 * e;
        double anchoBase = 104 * e;
        double altoCuerpo = 104 * e;

        double cima = cy - (altoCuello + altoCuerpo) / 2.0;
        double cuelloAbajo = cima + altoCuello;
        double base = cuelloAbajo + altoCuerpo;
        double nx1 = cx - anchoCuello / 2.0;
        double nx2 = cx + anchoCuello / 2.0;
        double bx1 = cx - anchoBase / 2.0;
        double bx2 = cx + anchoBase / 2.0;
        double esquina = anchoBase * 0.20;

        Path2D matraz = new Path2D.Double();
        matraz.moveTo(nx1, cima);
        matraz.lineTo(nx1, cuelloAbajo);
        matraz.lineTo(bx1, base - esquina);
        matraz.quadTo(bx1, base, bx1 + esquina, base);
        matraz.lineTo(bx2 - esquina, base);
        matraz.quadTo(bx2, base, bx2, base - esquina);
        matraz.lineTo(nx2, cuelloAbajo);
        matraz.lineTo(nx2, cima);
        matraz.closePath();

        // Fondo del matraz.
        g2.setColor(new Color(29, 91, 216, 28));
        g2.fill(matraz);

        // Líquido, recortado a la silueta del matraz.
        Shape clipAnterior = g2.getClip();
        g2.clip(matraz);
        g2.setColor(new Color(29, 91, 216, 130));
        double nivel = base - altoCuerpo * 0.45;
        g2.fill(new Rectangle2D.Double(cx - anchoBase, nivel, anchoBase * 2, base - nivel + 4));
        g2.setClip(clipAnterior);

        // Contorno y boca.
        g2.setColor(Tema.PRIMARIO);
        g2.setStroke(new BasicStroke((float) (2.6 * e), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(matraz);
        g2.setStroke(new BasicStroke((float) (3.4 * e), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Line2D.Double(nx1 - 6 * e, cima, nx2 + 6 * e, cima));

        // Burbujas.
        g2.setColor(new Color(29, 91, 216, 150));
        g2.fill(new Ellipse2D.Double(nx1 - 16 * e, cima - 30 * e, 8 * e, 8 * e));
        g2.fill(new Ellipse2D.Double(nx2 + 8 * e, cima - 48 * e, 6 * e, 6 * e));
        g2.fill(new Ellipse2D.Double(cx + 22 * e, cima - 22 * e, 5 * e, 5 * e));
    }
}
