package universidad.sigelab.view;

import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/** Igual que {@link CampoTexto}, pero oculta lo que se escribe. */
class CampoPassword extends JPasswordField {

    private int radio = 14;
    private int anchoPreferido;

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
        setBorder(new EmptyBorder(9, 12, 9, 12));
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
        base.height = Math.max(base.height, 40);
        return base;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean enfocado = isFocusOwner();
        g2.setColor(Tema.SUPERFICIE);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);

        g2.setColor(enfocado ? Tema.PRIMARIO : Tema.BORDE);
        g2.setStroke(new BasicStroke(enfocado ? 1.6f : 1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);
        g2.dispose();

        super.paintComponent(g);
    }
}
