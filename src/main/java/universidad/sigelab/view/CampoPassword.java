package universidad.sigelab.view;

import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CampoPassword extends JPasswordField {

    private String placeholder = "";
    private boolean mouseEncima = false;
    private final int marginGlow = 4;

    public CampoPassword(int columnas) {
        super(columnas);
        setForeground(Color.WHITE);
        setCaretColor(Tema.ETIQUETA);
        setBackground(Tema.PANEL);
        setFont(Tema.texto());
        setOpaque(false);
        setBorder(new EmptyBorder(10, 42, 10, 38));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseEncima = true;
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                mouseEncima = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int m = marginGlow;
        int x = m;
        int y = m;
        int w = getWidth() - (m * 2) - 1;
        int h = getHeight() - (m * 2) - 1;
        int radio = 12;

        // 1. Resplandor Neón
        if (mouseEncima || hasFocus()) {
            for (int i = m; i > 0; i--) {
                g2.setColor(new Color(0x00, 0xD8, 0xF6, 30 / i));
                g2.fillRoundRect(x - i, y - i, w + (i * 2), h + (i * 2), radio + (i * 2), radio + (i * 2));
            }
        }

        // 2. Fondo del campo
        g2.setColor(getBackground());
        g2.fillRoundRect(x, y, w, h, radio, radio);

        // 3. Borde Neón
        g2.setColor((hasFocus() || mouseEncima) ? Tema.ETIQUETA : Tema.BORDE);
        g2.drawRoundRect(x, y, w, h, radio, radio);

        // 4. Icono Candado (Izquierda)
        g2.setColor(Tema.ETIQUETA);
        int iconX = x + 14;
        int iconY = y + (h / 2);
        g2.drawRoundRect(iconX - 2, iconY - 2, 11, 9, 3, 3);
        g2.drawArc(iconX + 1, iconY - 7, 5, 7, 0, 180);

        // 5. Icono Ojo (Derecha)
        int eyeX = x + w - 22;
        g2.drawArc(eyeX - 4, iconY - 4, 13, 8, 0, 360);
        g2.fillOval(eyeX + 1, iconY - 2, 3, 3);

        super.paintComponent(g);

        // Placeholder seguro contra nulos
        if (getPassword().length == 0 && !hasFocus() && placeholder != null && !placeholder.isEmpty()) {
            g2.setColor(Tema.PLACEHOLDER);
            g2.setFont(getFont());
            int textY = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(placeholder, 44, textY);
        }

        g2.dispose();
    }

    public void setAnchoPreferido(int ancho) {
        setPreferredSize(new Dimension(ancho, 46));
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = (placeholder != null) ? placeholder : "";
        repaint();
    }

    public String getPlaceholder() {
        return placeholder;
    }
}