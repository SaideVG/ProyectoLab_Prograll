package universidad.sigelab.view;

import javax.swing.JButton;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BotonRedondeado extends JButton {

    public enum Estilo {
        PRIMARIO, SECUNDARIO, TEXTO
    }

    private final Estilo estilo;

    // Animación de resplandor neón
    private float alphaGlow = 0.4f;
    private float incrementoGlow = 0.02f;
    private final Timer timerGlow;

    private int desplazamientoFlecha = 0;
    private final Timer timerFlecha;
    private boolean mouseEncima = false;

    // Margen para dar espacio al resplandor y evitar que Swing lo corte
    private final int marginGlow = 6;

    public BotonRedondeado(String texto, Estilo estilo) {
        super(texto);
        this.estilo = estilo;
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setFont(Tema.boton());

        if (estilo == Estilo.PRIMARIO) {
            setBorder(new EmptyBorder(0, 20, 0, 40));
            setBackground(Tema.PRIMARIO);
            setForeground(new Color(0x03, 0x10, 0x1E));
        } else if (estilo == Estilo.SECUNDARIO) {
            setBorder(new EmptyBorder(0, 40, 0, 20));
            setBackground(Tema.AZUL_CLARO);
            setForeground(Tema.TEXTO);
        } else {
            setBorder(new EmptyBorder(0, 10, 0, 10));
            setBackground(new Color(0, 0, 0, 0));
            setForeground(Tema.TEXTO_SUAVE);
        }

        // Timer para la respiración de luz neón
        timerGlow = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                alphaGlow += incrementoGlow;
                if (alphaGlow >= 1.0f) {
                    alphaGlow = 1.0f;
                    incrementoGlow = -incrementoGlow;
                } else if (alphaGlow <= 0.3f) {
                    alphaGlow = 0.3f;
                    incrementoGlow = -incrementoGlow;
                }
                repaint();
            }
        });

        // Timer para desplazamiento suave de la flecha
        timerFlecha = new Timer(15, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mouseEncima) {
                    if (desplazamientoFlecha < 5) {
                        desplazamientoFlecha++;
                        repaint();
                    } else {
                        timerFlecha.stop();
                    }
                } else {
                    if (desplazamientoFlecha > 0) {
                        desplazamientoFlecha--;
                        repaint();
                    } else {
                        timerFlecha.stop();
                    }
                }
            }
        });

        if (estilo != Estilo.TEXTO) {
            timerGlow.start();
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseEncima = true;
                timerFlecha.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseEncima = false;
                timerFlecha.start();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int m = (estilo != Estilo.TEXTO) ? marginGlow : 0;
        int x = m;
        int y = m;
        int w = getWidth() - (m * 2);
        int h = getHeight() - (m * 2);
        int radio = 24;

        // 1. Resplandor Neón (Glow dibujado dentro de las fronteras visibles del componente)
        if (estilo != Estilo.TEXTO) {
            for (int i = m; i > 0; i--) {
                int alphaVal = (int) ((35 / i) * alphaGlow);
                g2.setColor(new Color(0x00, 0xD8, 0xF6, Math.min(255, alphaVal)));
                g2.fillRoundRect(x - i, y - i, w + (i * 2), h + (i * 2), radio + (i * 2), radio + (i * 2));
            }
        }

        // 2. Fondo y Vectores
        if (estilo == Estilo.PRIMARIO) {
            int r = (int) (0x00 + (0x00 * alphaGlow));
            int green = (int) (0xB0 + (0x28 * alphaGlow));
            int b = (int) (0xD0 + (0x26 * alphaGlow));

            Color colorRelleno = getModel().isRollover() ? Tema.PRIMARIO_HOVER : new Color(r, Math.min(255, green), Math.min(255, b));
            g2.setColor(colorRelleno);
            g2.fillRoundRect(x, y, w, h, radio, radio);

            // Flecha ➔
            g2.setColor(getForeground());
            int arrowX = x + w - 24 + desplazamientoFlecha;
            int arrowY = y + (h / 2);
            g2.drawLine(arrowX - 8, arrowY, arrowX, arrowY);
            g2.drawLine(arrowX - 3, arrowY - 3, arrowX, arrowY);
            g2.drawLine(arrowX - 3, arrowY + 3, arrowX, arrowY);

        } else if (estilo == Estilo.SECUNDARIO) {
            g2.setColor(getModel().isRollover() ? Tema.AZUL_CLARO_HOVER : getBackground());
            g2.fillRoundRect(x, y, w, h, radio, radio);

            int alphaBorde = (int) (120 + (135 * alphaGlow));
            g2.setColor(new Color(0x00, 0xD8, 0xF6, Math.min(255, alphaBorde)));
            g2.drawRoundRect(x, y, w, h, radio, radio);

            // Silueta Usuario 👤
            g2.setColor(Tema.ETIQUETA);
            int userX = x + 18;
            int userY = y + (h / 2);
            g2.drawOval(userX, userY - 7, 6, 6);
            g2.drawArc(userX - 3, userY, 12, 8, 0, 180);
        }

        super.paintComponent(g);
        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, 52);
    }

    public void destello() {
    }
}