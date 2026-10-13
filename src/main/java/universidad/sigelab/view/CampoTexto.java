package universidad.sigelab.view;

import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Campo de texto con fondo claro redondeado, borde azul al enfocar y texto de
 * ayuda (placeholder) cuando está vacío.
 */
class CampoTexto extends JTextField {

    private int radio = 12;
    private int anchoPreferido;
    private String placeholder;

    CampoTexto(int columnas) {
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
        setBorder(new EmptyBorder(10, 12, 10, 12));
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
    }

    void setRadio(int radio) {
        this.radio = radio;
        repaint();
    }

    void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    /** Fuerza un ancho concreto: JTextField ignora el ancho de setPreferredSize(). */
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

        if (placeholder != null && getText().isEmpty()) {
            pintarPlaceholder(g);
        }
    }

    /** Dibuja el texto de ayuda en gris cuando el campo está vacío. */
    void pintarPlaceholder(Graphics g) {
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
}
