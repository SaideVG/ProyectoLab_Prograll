package universidad.sigelab.view;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

public class PanelIlustracion extends JPanel {

    private final ImageIcon logoNube;

    public PanelIlustracion() {
        setOpaque(false);
        this.logoNube = cargarLogoNube();
    }

    private ImageIcon cargarLogoNube() {
        String[] rutas = {
                "/images/logo_nube.png.png",
                "/images/logo_nube.png",
                "/images/logo_sigelab.png.png",
                "/images/logo_sigelab.png"
        };
        for (String r : rutas) {
            URL url = getClass().getResource(r);
            if (url != null) return recortarMargenes(new ImageIcon(url));
        }
        File f1 = new File("src/main/resources/images/logo_nube.png.png");
        if (f1.exists()) return recortarMargenes(new ImageIcon(f1.getAbsolutePath()));
        File f2 = new File("src/main/resources/images/logo_nube.png");
        if (f2.exists()) return recortarMargenes(new ImageIcon(f2.getAbsolutePath()));
        return null;
    }

    /**
     * Elimina el margen transparente que rodea al dibujo, para poder escalar el
     * logo por su contenido real en lugar del lienzo original.
     */
    private ImageIcon recortarMargenes(ImageIcon icono) {
        try {
            int w = icono.getIconWidth();
            int h = icono.getIconHeight();
            if (w <= 0 || h <= 0) return icono;

            BufferedImage origen = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = origen.createGraphics();
            g.drawImage(icono.getImage(), 0, 0, null);
            g.dispose();

            int[] pixeles = origen.getRGB(0, 0, w, h, null, 0, w);
            int minX = w, minY = h, maxX = -1, maxY = -1;
            for (int y = 0; y < h; y++) {
                int fila = y * w;
                for (int x = 0; x < w; x++) {
                    if ((pixeles[fila + x] >>> 24) > 12) {
                        if (x < minX) minX = x;
                        if (x > maxX) maxX = x;
                        if (y < minY) minY = y;
                        if (y > maxY) maxY = y;
                    }
                }
            }
            if (maxX < minX || maxY < minY) return icono; // imagen vacía

            BufferedImage recorte = origen.getSubimage(
                    minX, minY, maxX - minX + 1, maxY - minY + 1);
            return new ImageIcon(recorte);
        } catch (Exception e) {
            return icono;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        int ancho = getWidth();
        int alto = getHeight();
        float alpha = LoginForm.getAlphaGlobal();

        // Fondo columna derecha
        g2.setColor(new Color(0x06, 0x0B, 0x14));
        g2.fillRect(0, 0, ancho, alto);

        // Orbes decorativos de fondo
        g2.setColor(new Color(0x0C, 0x1E, 0x38, 120));
        g2.fillOval(ancho - 150, -80, 320, 320);

        g2.setColor(new Color(0x0B, 0x22, 0x3E, 100));
        g2.fillOval(ancho - 220, alto - 240, 380, 380);

        // Tarjeta central flotante
        int tarjetaW = 340;
        int tarjetaH = 340;
        int tarjetaX = (ancho - tarjetaW) / 2;
        int tarjetaY = (alto - tarjetaH) / 2 - 25;

        g2.setColor(new Color(0x0C, 0x16, 0x2A));
        g2.fillRoundRect(tarjetaX, tarjetaY, tarjetaW, tarjetaH, 20, 20);

        g2.setColor(Tema.BORDE);
        g2.drawRoundRect(tarjetaX, tarjetaY, tarjetaW, tarjetaH, 20, 20);

        int cx = tarjetaX + (tarjetaW / 2);

        // Dimensiones del logo ya recortado (proporción real del contenido)
        int imgW = tarjetaW - 40;
        int imgH = 200;
        if (logoNube != null && logoNube.getIconWidth() > 0) {
            imgH = (int) ((double) logoNube.getIconHeight() / logoNube.getIconWidth() * imgW);
            if (imgH > 210) {
                imgH = 210;
                imgW = (int) ((double) logoNube.getIconWidth() / logoNube.getIconHeight() * imgH);
            }
        }
        int imgX = cx - (imgW / 2);
        // Centrado en el espacio libre que queda encima del badge
        int imgY = tarjetaY + (tarjetaH - 60 - imgH) / 2;
        int cy = imgY + (imgH / 2);

        // Resplandor neón cian animado en el fondo del logo
        int radioGlow = (Math.max(imgW, imgH) / 2) + 10;
        for (int r = radioGlow; r > 0; r -= 5) {
            g2.setColor(new Color(0x00, 0xD8, 0xF6, (int) ((18 / (r / 5.0)) * alpha)));
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        }

        // Dibujo del Logo de la Nube
        if (logoNube != null && logoNube.getImage() != null) {
            g2.drawImage(logoNube.getImage(), imgX, imgY, imgW, imgH, this);
        }

        // BADGE: VINCULADO A SQL SERVER
        int badgeW = 210;
        int badgeH = 34;
        int badgeX = cx - (badgeW / 2);
        int badgeY = tarjetaY + tarjetaH - 60;

        for (int i = 5; i > 0; i--) {
            g2.setColor(new Color(0x10, 0xB9, 0x81, (int) ((25 / i) * alpha)));
            g2.fillRoundRect(badgeX - i, badgeY - i, badgeW + (i * 2), badgeH + (i * 2), 16 + i, 16 + i);
        }

        g2.setColor(new Color(0x06, 0x1E, 0x22));
        g2.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 16, 16);

        int alphaBorde = (int) (120 + (135 * alpha));
        g2.setColor(new Color(0x10, 0xB9, 0x81, Math.min(255, alphaBorde)));
        g2.drawRoundRect(badgeX, badgeY, badgeW, badgeH, 16, 16);

        // Indicador verde activo
        g2.setColor(new Color(0x10, 0xB9, 0x81));
        g2.fillOval(badgeX + 16, badgeY + 12, 10, 10);

        // Texto actualizado
        g2.setColor(new Color(0x34, 0xD3, 0x99));
        g2.setFont(Tema.etiqueta());
        g2.drawString("Vinculado a SQL Server", badgeX + 34, badgeY + 22);

        // Textos del pie
        String line1 = "Verificación de identidad estudiantil y asignación";
        String line2 = "de puestos de práctica en tiempo real.";
        g2.setColor(Tema.TEXTO_SUAVE);
        g2.setFont(Tema.pie());
        g2.drawString(line1, cx - (g2.getFontMetrics().stringWidth(line1) / 2), tarjetaY + tarjetaH + 28);
        g2.drawString(line2, cx - (g2.getFontMetrics().stringWidth(line2) / 2), tarjetaY + tarjetaH + 42);

        String foot = "Dirección General de Laboratorios e Informática";
        g2.setColor(Tema.PIE);
        g2.drawString(foot, cx - (g2.getFontMetrics().stringWidth(foot) / 2), alto - 25);

        g2.dispose();
    }
}