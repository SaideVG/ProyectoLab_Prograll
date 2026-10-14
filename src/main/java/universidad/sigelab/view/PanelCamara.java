package universidad.sigelab.view;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import universidad.sigelab.service.ReconocimientoFacial;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Vista previa de la cámara con detección de rostro en vivo.
 *
 * La captura corre en un hilo aparte para no congelar la interfaz: se leen los
 * fotogramas y se dibujan de inmediato, y la detección de rostro (que es lo más
 * costoso) se hace cada pocos fotogramas sobre una copia reducida. Así la vista
 * previa se mantiene fluida y se aprecia el movimiento del rostro.
 *
 * El fotograma y su detección se guardan siempre juntos dentro del mismo candado,
 * porque las clases nativas de OpenCV no son seguras entre hilos.
 */
class PanelCamara extends JPanel {

    private static final int ANCHO = 440;
    private static final int ALTO = 330;

    /** Se busca el rostro cada tantos fotogramas; los demás solo se muestran. */
    private static final int FOTOGRAMAS_POR_DETECCION = 2;

    private final VideoCapture camara = new VideoCapture();
    private final Object candado = new Object();
    private final AtomicBoolean repintadoPendiente = new AtomicBoolean();

    private Thread hilo;
    private volatile boolean activo;
    private volatile boolean camaraDisponible;
    private volatile BufferedImage imagen;

    private Mat fotograma;   // último fotograma BGR (protegido por candado)
    private Mat cara;        // fila 1x15 del rostro detectado, o null

    PanelCamara() {
        setPreferredSize(new Dimension(ANCHO, ALTO));
        setMinimumSize(new Dimension(ANCHO, ALTO));
        setBackground(new Color(0x0F172A));
        setOpaque(true);
    }

    /** Abre la cámara. Devuelve false si no hay ninguna disponible. */
    boolean abrir() {
        ReconocimientoFacial.inicializar();
        camaraDisponible = camara.open(0, Videoio.CAP_DSHOW);
        if (!camaraDisponible) camaraDisponible = camara.open(0);
        if (camaraDisponible) {
            camara.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
            camara.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
            // Se pide a la cámara que guarde pocos fotogramas, para que lo que se ve
            // sea lo más reciente posible y no vaya con retraso.
            camara.set(Videoio.CAP_PROP_BUFFERSIZE, 1);
        }
        return camaraDisponible;
    }

    void iniciar() {
        if (!camaraDisponible || hilo != null) return;
        activo = true;
        hilo = new Thread(this::bucle, "sigelab-camara");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void bucle() {
        Mat lectura = new Mat();
        int contador = 0;
        while (activo) {
            // read() ya espera al siguiente fotograma, así que no hace falta pausar.
            if (!camara.isOpened() || !camara.read(lectura) || lectura.empty()) {
                dormir(30);
                continue;
            }

            Mat copia = lectura.clone();
            BufferedImage cuadro = aImagen(copia);

            // La detección se hace fuera del candado para no frenar el dibujado.
            boolean tocaDetectar = contador % FOTOGRAMAS_POR_DETECCION == 0;
            Mat rostro = tocaDetectar ? ReconocimientoFacial.detectar(copia) : null;

            synchronized (candado) {
                if (tocaDetectar) {
                    if (cara != null) cara.release();
                    cara = rostro;
                }
                if (fotograma != null) fotograma.release();
                fotograma = copia;
            }

            imagen = cuadro;
            // Solo se pide un repintado si no hay otro en cola: así la interfaz no se
            // satura y siempre dibuja el fotograma más reciente.
            if (repintadoPendiente.compareAndSet(false, true)) {
                SwingUtilities.invokeLater(() -> {
                    repintadoPendiente.set(false);
                    repaint();
                });
            }
            contador++;
        }
        lectura.release();
        // Al terminar, se liberan el último fotograma y la última detección (dejando
        // cerrar() y esto protegidos por el mismo candado para no liberar dos veces).
        synchronized (candado) {
            if (fotograma != null) {
                fotograma.release();
                fotograma = null;
            }
            if (cara != null) {
                cara.release();
                cara = null;
            }
        }
    }

    /**
     * Detecta el rostro en el último fotograma y devuelve su firma facial.
     * Devuelve {@code null} si no hay ningún rostro visible.
     */
    float[] vectorDelRostro() {
        Mat copia;
        synchronized (candado) {
            if (fotograma == null) return null;
            copia = fotograma.clone();
        }
        try {
            Mat rostro = ReconocimientoFacial.detectar(copia);
            if (rostro == null) return null;
            return ReconocimientoFacial.vectorDe(copia, rostro);
        } finally {
            copia.release();
        }
    }

    boolean hayRostro() {
        synchronized (candado) {
            return cara != null;
        }
    }

    void cerrar() {
        activo = false;
        if (camara.isOpened()) camara.release();
        synchronized (candado) {
            if (fotograma != null) {
                fotograma.release();
                fotograma = null;
            }
            if (cara != null) {
                cara.release();
                cara = null;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        BufferedImage cuadro = imagen;
        if (cuadro != null) {
            // Se escala manteniendo la proporción y se centra; así nada se deforma.
            double escala = Math.max((double) getWidth() / cuadro.getWidth(), (double) getHeight() / cuadro.getHeight());
            int ancho = (int) Math.round(cuadro.getWidth() * escala);
            int alto = (int) Math.round(cuadro.getHeight() * escala);
            int x = (getWidth() - ancho) / 2;
            int y = (getHeight() - alto) / 2;
            g2.drawImage(cuadro, x, y, ancho, alto, null);

            synchronized (candado) {
                if (cara != null) {
                    Rect r = ReconocimientoFacial.recuadro(cara);
                    if (r != null) {
                        g2.setColor(Tema.EXITO);
                        g2.setStroke(new BasicStroke(3f));
                        g2.drawRoundRect(x + (int) (r.x * escala), y + (int) (r.y * escala),
                                (int) (r.width * escala), (int) (r.height * escala), 18, 18);
                    }
                }
            }
        } else {
            g2.setColor(Color.WHITE);
            g2.setFont(Tema.subtitulo());
            String texto = camaraDisponible ? "Iniciando cámara…" : "No se detectó ninguna cámara en este equipo";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(texto, (getWidth() - fm.stringWidth(texto)) / 2, getHeight() / 2);
        }
        g2.dispose();
    }

    /**
     * Convierte el fotograma BGR de OpenCV a una imagen con los colores reales.
     *
     * Se empaqueta cada píxel a mano (B, G, R → rojo, verde, azul) en vez de usar
     * el raster directamente, que según la máquina puede intercambiar o invertir
     * los canales. Es la forma que garantiza colores fieles.
     */
    private static BufferedImage aImagen(Mat bgr) {
        int ancho = bgr.cols();
        int alto = bgr.rows();
        byte[] datos = new byte[ancho * alto * 3];
        bgr.get(0, 0, datos);

        int[] pixeles = new int[ancho * alto];
        for (int i = 0, j = 0; i < pixeles.length; i++, j += 3) {
            int azul = datos[j] & 0xFF;
            int verde = datos[j + 1] & 0xFF;
            int rojo = datos[j + 2] & 0xFF;
            pixeles[i] = 0xFF000000 | (rojo << 16) | (verde << 8) | azul;
        }

        BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        resultado.setRGB(0, 0, ancho, alto, pixeles, 0, ancho);
        return resultado;
    }

    private static void dormir(long milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
