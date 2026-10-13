package universidad.sigelab.view;

import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Igual que {@link CampoTexto}, pero oculta lo que se escribe.
 *
 * Incluye un "ojito" a la derecha que permite mostrar u ocultar la contraseña
 * con un clic.
 */
class CampoPassword extends JPasswordField {

    /** Carácter que se dibuja en lugar de cada letra de la contraseña. */
    private static final char OCULTO = '\u2022';
    private static final int ZONA_OJO = 38;

    private int radio = 12;
    private int anchoPreferido;
    private String placeholder;
    private boolean visible;

    CampoPassword(int columnas) {
        super(columnas);
        inicializar();
    }

    private void inicializar() {
        setOpaque(false);
        setFont(Tema.texto());
        setForeground(Tema.TEXTO);
        setCaretColor(Tema.PRIMARIO);
        setSelectionColor(Tema.AZUL_CLARO);
        setSelectedTextColor(Tema.TEXTO);
        setEchoChar(OCULTO);
        setBorder(new EmptyBorder(10, 12, 10, ZONA_OJO + 6));
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
        MouseAdapter raton = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (sobreElOjo(e.getX())) {
                    alternarVisibilidad();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                setCursor(sobreElOjo(e.getX())
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            }
        };
        addMouseListener(raton);
        addMouseMotionListener(raton);
    }

    private boolean sobreElOjo(int x) {
        return x >= getWidth() - ZONA_OJO;
    }

    /** Muestra u oculta la contraseña. */
    void alternarVisibilidad() {
        visible = !visible;
        setEchoChar(visible ? (char) 0 : OCULTO);
        repaint();
    }

    void setRadio(int radio) {
        this.radio = radio;
        repaint();
    }

    void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    /** Fuerza un ancho concreto: JPasswordField ignora el ancho de setPreferredSize(). */
    void setAnchoPreferido(int anchoPreferido) {
        this.anchoPreferido = anchoPreferido;
        revalidate();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension base = super.getPreferredSize();
        if (anchoPreferido > 0) {
            base.width = anchoPreferido;
        }
        base.height = Math.max(base.height, 44);
        return base;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean enfocado = isFocusOwner();
        g2.setColor(Tema.SUPERFICIE_ALT);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);

        g2.setColor(enfocado ? Tema.PRIMARIO : Tema.BORDE_CAMPO);
        g2.setStroke(new BasicStroke(enfocado ? 1.6f : 1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);
        g2.dispose();

        super.paintComponent(g);

        if (placeholder != null && getPassword().length == 0) {
            pintarPlaceholder(g);
        }
        pintarOjo(g);
    }

    /** Dibuja el texto de ayuda en gris cuando el campo está vacío. */
    private void pintarPlaceholder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(getFont());
        g2.setColor(Tema.PLACEHOLDER);
        FontMetrics fm = g2.getFontMetrics();
        int x = getInsets().left;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(placeholder, x, y);
        g2.dispose();
    }

    /** Dibuja el icono del ojo (con una barra cuando está oculta). */
    private void pintarOjo(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = getWidth() - ZONA_OJO / 2 - 4;
        int cy = getHeight() / 2;
        int ancho = 20;
        int alto = 11;

        g2.setColor(visible ? Tema.PRIMARIO : Tema.TEXTO_SUAVE);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // Contorno del ojo: dos arcos que forman una almendra.
        g2.drawArc(cx - ancho / 2, cy - alto / 2, ancho, alto, 0, 180);
        g2.drawArc(cx - ancho / 2, cy - alto / 2, ancho, alto, 180, 180);
        // Pupila.
        g2.fillOval(cx - 2, cy - 2, 5, 5);
        // Barra cuando la contraseña está oculta.
        if (!visible) {
            g2.drawLine(cx - 9, cy + 6, cx + 9, cy - 6);
        }
        g2.dispose();
    }
}
