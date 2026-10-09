package universidad.sigelab.view;

import javax.swing.JButton;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Botón con esquinas redondeadas y animación.
 *
 * Tiene tres estados visuales (normal, hover y presionado) y una pequeña
 * animación de escala al pasar el mouse y al presionar. El método
 * {@link #destello()} hace un rebote más notorio (se usa en "Registro nuevo").
 */
class BotonRedondeado extends JButton {

    /** Variantes de color para no repetir colores en cada pantalla. */
    enum Estilo { PRIMARIO, SECUNDARIO, CLARO, PELIGRO }

    private Color fondo;
    private Color fondoHover;
    private Color fondoPresionado;
    private Color colorTexto;
    private Color colorBorde;

    private int radio = 16;
    private boolean sobreElBoton;
    private boolean presionado;
    private float escala = 1f;
    private Timer animacion;

    BotonRedondeado(String texto) {
        this(texto, Estilo.PRIMARIO);
    }

    BotonRedondeado(String texto, Estilo estilo) {
        super(texto);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setFont(Tema.boton());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(0, 0, 0, 0));
        setRolloverEnabled(false);

        aplicarEstilo(estilo);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                sobreElBoton = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                sobreElBoton = false;
                presionado = false;
                animarA(1f);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    presionado = true;
                    animarA(0.95f);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                presionado = false;
                animarA(1f);
            }
        });
    }

    final void aplicarEstilo(Estilo estilo) {
        switch (estilo) {
            case PRIMARIO -> {
                fondo = Tema.PRIMARIO;
                fondoHover = Tema.PRIMARIO_HOVER;
                fondoPresionado = Tema.PRIMARIO_PRESIONADO;
                colorTexto = Color.WHITE;
                colorBorde = null;
            }
            case SECUNDARIO -> {
                fondo = Tema.SUPERFICIE;
                fondoHover = Tema.AZUL_CLARO;
                fondoPresionado = Tema.AZUL_CLARO_HOVER;
                colorTexto = Tema.PRIMARIO;
                colorBorde = Tema.PRIMARIO;
            }
            case CLARO -> {
                fondo = Tema.AZUL_CLARO;
                fondoHover = Tema.AZUL_CLARO_HOVER;
                fondoPresionado = Tema.AZUL_CLARO_HOVER;
                colorTexto = Tema.PRIMARIO;
                colorBorde = null;
            }
            case PELIGRO -> {
                fondo = Tema.PELIGRO;
                fondoHover = Tema.PELIGRO_HOVER;
                fondoPresionado = Tema.PELIGRO_PRESIONADO;
                colorTexto = Color.WHITE;
                colorBorde = null;
            }
        }
        repaint();
    }

    void setRadio(int radio) {
        this.radio = radio;
        repaint();
    }

    /** Animación breve: encoge un poco y vuelve al tamaño normal. */
    private void animarA(float destino) {
        if (animacion != null) animacion.stop();
        animacion = new Timer(12, e -> {
            float paso = 0.05f;
            if (Math.abs(escala - destino) <= paso) {
                escala = destino;
                ((Timer) e.getSource()).stop();
            } else {
                escala += destino > escala ? paso : -paso;
            }
            repaint();
        });
        animacion.start();
    }

    /**
     * Rebote para llamar la atención cuando se da clic.
     * Se conserva dentro de los límites del botón para que no se recorte.
     */
    void destello() {
        if (animacion != null) animacion.stop();
        final float[] secuencia = {1f, 0.90f, 1.02f, 1f};
        final int[] indice = {0};
        animacion = new Timer(60, e -> {
            escala = secuencia[indice[0]];
            repaint();
            indice[0]++;
            if (indice[0] >= secuencia.length) {
                ((Timer) e.getSource()).stop();
            }
        });
        animacion.setInitialDelay(0);
        animacion.start();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension base = super.getPreferredSize();
        return new Dimension(base.width + 30, Math.max(42, base.height + 16));
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ancho = getWidth();
        int alto = getHeight();
        int anchoPintado = Math.round(ancho * escala);
        int altoPintado = Math.round(alto * escala);
        int x = (ancho - anchoPintado) / 2;
        int y = (alto - altoPintado) / 2;

        Color relleno;
        if (!isEnabled()) {
            relleno = Tema.DESHABILITADO;
        } else if (presionado) {
            relleno = fondoPresionado;
        } else if (sobreElBoton) {
            relleno = fondoHover;
        } else {
            relleno = fondo;
        }

        g2.setColor(relleno);
        g2.fillRoundRect(x, y, anchoPintado - 1, altoPintado - 1, radio, radio);

        if (colorBorde != null && isEnabled()) {
            g2.setColor(colorBorde);
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(x, y, anchoPintado - 1, altoPintado - 1, radio, radio);
        }

        String texto = getText();
        g2.setFont(getFont());
        g2.setColor(isEnabled() ? colorTexto : Tema.DESHABILITADO_TEXTO);
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (anchoPintado - fm.stringWidth(texto)) / 2;
        int ty = y + (altoPintado - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(texto, tx, ty);

        g2.dispose();
    }
}
