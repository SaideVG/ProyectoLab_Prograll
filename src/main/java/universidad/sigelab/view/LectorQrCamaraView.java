package universidad.sigelab.view;

import universidad.sigelab.controller.QrController;
import universidad.sigelab.util.BusinessException;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Ventana que muestra lo que ve la cámara y espera hasta que aparece un código QR.
 */
public class LectorQrCamaraView {

    private static final int PAUSA_ENTRE_CAPTURAS_MS = 100;

    // Componentes vinculados desde LectorQrCamaraView.form
    private JPanel panelPrincipal;
    private JComboBox<String> cboCamara;
    private JLabel lblVideo;
    private JLabel lblEstado;
    private JButton btnCancelar;

    private final QrController qrController;

    private JDialog ventana;
    private volatile String camaraElegida;
    private SwingWorker<String, BufferedImage> lector;
    private String codigoLeido;
    private Exception error;

    public LectorQrCamaraView(QrController qrController) {
        this.qrController = qrController;
    }

    public String leer(Component padre) {
        List<String> camaras = qrController.listarCamaras();
        if (camaras == null || camaras.isEmpty()) {
            throw new BusinessException("No se detectó ninguna cámara en este equipo. " +
                    "Puede leer el QR desde una imagen.");
        }

        configurarFormulario(camaras);
        configurarEventos();

        ventana = new JDialog(SwingUtilities.getWindowAncestor(padre), "Leer código QR con la cámara",
                Dialog.ModalityType.APPLICATION_MODAL);
        ventana.setContentPane(panelPrincipal);
        ventana.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        ventana.setSize(700, 640);
        ventana.setLocationRelativeTo(padre);

        ventana.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (lector != null) {
                    lector.cancel(false);
                }
            }
        });

        iniciarLectura();
        ventana.setVisible(true);

        if (error instanceof RuntimeException problema) throw problema;
        if (error != null) throw new IllegalStateException(error);
        return codigoLeido;
    }

    private void configurarFormulario(List<String> camaras) {
        if (panelPrincipal != null) {
            panelPrincipal.setBackground(new Color(0x06, 0x0B, 0x14));

            for (Component c : panelPrincipal.getComponents()) {
                if (c instanceof JLabel lbl) {
                    lbl.setForeground(Color.WHITE);
                    lbl.setFont(Tema.texto());
                } else if (c instanceof JPanel pnl) {
                    pnl.setBackground(new Color(0x06, 0x0B, 0x14));
                    for (Component sub : pnl.getComponents()) {
                        if (sub instanceof JLabel subLbl) {
                            subLbl.setForeground(Color.WHITE);
                            subLbl.setFont(Tema.texto());
                        }
                    }
                }
            }
        }

        cboCamara.removeAllItems();
        for (String camara : camaras) {
            cboCamara.addItem(camara);
        }

        estilizarComboBoxPaleta(cboCamara);

        camaraElegida = camaras.get(0);

        lblVideo.setHorizontalAlignment(SwingConstants.CENTER);
        lblVideo.setText("Encendiendo la cámara...");
        lblVideo.setForeground(Color.WHITE);
        lblVideo.setFont(Tema.subtitulo());

        lblEstado.setText("Coloque el código QR frente a la cámara.");
        lblEstado.setForeground(Tema.TEXTO_SUAVE);
        lblEstado.setFont(Tema.texto());

        if (btnCancelar != null) {
            btnCancelar.setBackground(new Color(0x0A, 0x12, 0x24));
            btnCancelar.setForeground(Tema.TEXTO_SUAVE);
            btnCancelar.setFocusPainted(false);
            btnCancelar.setFont(Tema.texto());
            btnCancelar.setBorder(new LineBorder(Tema.BORDE, 1, true));
        }
    }

    /**
     * Reemplaza el UI para forzar el fondo oscuro y pintar el texto directamente en el canal gráfico.
     */
    private void estilizarComboBoxPaleta(JComboBox<String> combo) {
        if (combo == null) return;

        Color fondoOscuro = new Color(0x0A, 0x12, 0x24);

        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new BasicArrowButton(BasicArrowButton.SOUTH, fondoOscuro, fondoOscuro, Tema.PRIMARIO, fondoOscuro);
                btn.setBorder(new EmptyBorder(0, 4, 0, 8));
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(fondoOscuro);
                g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                g2.dispose();
            }

            @Override
            public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                String texto = (String) combo.getSelectedItem();
                if (texto == null) return;

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(Tema.texto());
                g2.setColor(Tema.PRIMARIO);

                FontMetrics fm = g2.getFontMetrics();
                int y = bounds.y + ((bounds.height - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(texto, bounds.x + 8, y);
                g2.dispose();
            }
        });

        combo.setBackground(fondoOscuro);
        combo.setForeground(Tema.PRIMARIO);
        combo.setFont(Tema.texto());
        combo.setBorder(new LineBorder(Tema.BORDE, 1, true));

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JComponent comp = (JComponent) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                comp.setFont(Tema.texto());
                comp.setBorder(new EmptyBorder(6, 10, 6, 10));

                if (isSelected) {
                    comp.setBackground(Tema.PRIMARIO);
                    comp.setForeground(Color.WHITE);
                } else {
                    comp.setBackground(fondoOscuro);
                    comp.setForeground(Color.WHITE);
                }
                return comp;
            }
        });
    }

    private void configurarEventos() {
        cboCamara.addActionListener(e -> {
            camaraElegida = (String) cboCamara.getSelectedItem();
            lblVideo.setIcon(null);
            lblVideo.setText("Encendiendo la cámara...");
        });

        btnCancelar.addActionListener(e -> ventana.dispose());
    }

    private void iniciarLectura() {
        lector = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                String camaraEncendida = null;
                try {
                    while (!isCancelled()) {
                        String elegida = camaraElegida;
                        if (elegida != null && !elegida.equals(camaraEncendida)) {
                            qrController.abrirCamara(elegida);
                            camaraEncendida = elegida;
                        }

                        BufferedImage imagen = qrController.capturarImagen();
                        if (imagen != null) {
                            publish(imagen);
                            Optional<String> codigo = qrController.buscarCodigo(imagen);
                            if (codigo.isPresent()) return codigo.get();
                        }

                        Thread.sleep(PAUSA_ENTRE_CAPTURAS_MS);
                    }
                    return null;
                } finally {
                    qrController.cerrarCamara();
                }
            }

            @Override
            protected void process(List<BufferedImage> imagenes) {
                lblVideo.setText(null);
                lblVideo.setIcon(new ImageIcon(imagenes.get(imagenes.size() - 1)));
            }

            @Override
            protected void done() {
                if (isCancelled()) return;

                try {
                    codigoLeido = get();
                } catch (ExecutionException e) {
                    error = e.getCause() instanceof Exception causa ? causa : e;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (ventana != null) {
                    ventana.dispose();
                }
            }
        };

        lector.execute();
    }
}