package universidad.sigelab.view;

import javax.swing.JLabel;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Etiqueta que se comporta como un enlace: subraya al pasar el mouse y ejecuta una acción. */
class Enlace extends JLabel {

    Enlace(String texto, Runnable accion) {
        super(texto);
        setFont(Tema.enlace());
        setForeground(Tema.PRIMARIO);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setText("<html><u>" + texto + "</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setText(texto);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (accion != null) {
                    accion.run();
                }
            }
        });
    }
}
