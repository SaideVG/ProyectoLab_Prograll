package universidad.sigelab.service;

import nu.pattern.OpenCV;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.FaceDetectorYN;
import org.opencv.objdetect.FaceRecognizerSF;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Reconocimiento facial (RN-02) apoyado en OpenCV.
 *
 * Usa dos modelos que viajan dentro del proyecto (carpeta {@code rostro}):
 * <ul>
 *   <li>YuNet  (face_detection_yunet_2023mar.onnx):  detecta el rostro.</li>
 *   <li>SFace  (face_recognition_sface_2021dec.onnx): calcula la firma facial.</li>
 * </ul>
 *
 * La firma facial es un vector de 128 números float. Dos firmas se comparan con
 * la similitud del coseno: si es &gt;= {@link #UMBRAL_SIMILITUD} se consideran la
 * misma persona.
 *
 * Las clases nativas de OpenCV no son seguras entre hilos, por eso todos los
 * métodos que las usan están sincronizados.
 */
public final class ReconocimientoFacial {

    /** Umbral recomendado por OpenCV para SFace con similitud del coseno. */
    public static final double UMBRAL_SIMILITUD = 0.363;

    /** SFace devuelve 128 números float; en la base de datos ocupan 512 bytes. */
    public static final int LARGO_VECTOR = 128;

    private static final String MODELO_DETECCION = "face_detection_yunet_2023mar.onnx";
    private static final String MODELO_RECONOCIMIENTO = "face_recognition_sface_2021dec.onnx";
    private static final float UMBRAL_DETECCION = 0.9f;
    private static final float UMBRAL_NMS = 0.3f;
    private static final int TOP_K = 5000;

    /**
     * Lado mayor, en píxeles, de la copia sobre la que se busca el rostro. La
     * detección es lo más costoso del proceso, así que se hace en pequeño y
     * luego el recuadro se devuelve a las coordenadas de la imagen original.
     */
    private static final int LADO_DETECCION = 320;

    private static boolean intentado;
    private static boolean disponible;
    private static String error;
    private static FaceDetectorYN detector;
    private static FaceRecognizerSF reconocedor;

    private ReconocimientoFacial() { }

    /** Carga OpenCV y los modelos. Es idempotente y se puede llamar desde varios hilos. */
    public static synchronized void inicializar() {
        if (intentado) return;
        intentado = true;
        try {
            OpenCV.loadLocally();
            detector = FaceDetectorYN.create(rutaModelo(MODELO_DETECCION), "",
                    new Size(320, 320), UMBRAL_DETECCION, UMBRAL_NMS, TOP_K);
            reconocedor = FaceRecognizerSF.create(rutaModelo(MODELO_RECONOCIMIENTO), "");
            disponible = true;
        } catch (Throwable e) {
            disponible = false;
            error = e.getMessage();
            e.printStackTrace();
        }
    }

    /** Adelanta la carga en segundo plano para que el primer login no espere. */
    public static void precargar() {
        Thread hilo = new Thread(ReconocimientoFacial::inicializar, "sigelab-opencv");
        hilo.setDaemon(true);
        hilo.start();
    }

    public static synchronized boolean disponible() {
        inicializar();
        return disponible;
    }

    public static synchronized String error() {
        inicializar();
        return error;
    }

    /**
     * Detecta el primer rostro de la imagen.
     *
     * Para que la vista previa sea fluida, la búsqueda se hace sobre una copia
     * reducida (lado mayor {@link #LADO_DETECCION}) y luego el recuadro y los
     * puntos se devuelven a las coordenadas de la imagen original. Devuelve la
     * fila de detección (1x15: x, y, ancho, alto y los 5 puntos del rostro) o
     * {@code null} si no encuentra ninguno.
     */
    public static synchronized Mat detectar(Mat imagenBgr) {
        inicializar();
        if (!disponible || imagenBgr == null || imagenBgr.empty()) return null;

        int ancho = imagenBgr.cols();
        int alto = imagenBgr.rows();

        // La red trabaja mejor con lados múltiplos de 32; se redondea y luego se
        // deshace la escala con el factor real de cada eje.
        double factor = Math.min(1.0, LADO_DETECCION / (double) Math.max(ancho, alto));
        int anchoBusqueda = Math.max(32, (int) Math.round(ancho * factor / 32.0) * 32);
        int altoBusqueda = Math.max(32, (int) Math.round(alto * factor / 32.0) * 32);

        Mat busqueda = new Mat();
        Mat caras = new Mat();
        try {
            Imgproc.resize(imagenBgr, busqueda, new Size(anchoBusqueda, altoBusqueda));

            detector.setInputSize(new Size(anchoBusqueda, altoBusqueda));
            detector.detect(busqueda, caras);
            // El valor que devuelve detect() no es fiable en esta versión (devuelve 1
            // aunque no encuentre nada), así que se mira el contenido del resultado.
            if (caras.empty() || caras.rows() < 1) return null;

            Mat cara = caras.row(0).clone();
            if (anchoBusqueda == ancho && altoBusqueda == alto) return cara;

            // Se devuelven el recuadro y los 5 puntos a las coordenadas originales.
            // La fila de detección es CV_32F, así que se lee como float[].
            double escalaX = ancho / (double) anchoBusqueda;
            double escalaY = alto / (double) altoBusqueda;
            float[] datos = new float[15];
            cara.get(0, 0, datos);
            cara.release();

            float[] escalados = new float[15];
            for (int i = 0; i < 15; i++) {
                if (i == 14) {
                    escalados[i] = datos[i];
                } else if (i % 2 == 0) {
                    escalados[i] = (float) (datos[i] * escalaX);
                } else {
                    escalados[i] = (float) (datos[i] * escalaY);
                }
            }
            Mat resultado = new Mat(1, 15, CvType.CV_32F);
            resultado.put(0, 0, escalados);
            return resultado;
        } finally {
            caras.release();
            busqueda.release();
        }
    }

    /** Calcula la firma facial de un rostro ya detectado dentro de la imagen. */
    public static synchronized float[] vectorDe(Mat imagenBgr, Mat cara) {
        inicializar();
        if (!disponible) {
            throw new IllegalStateException("El reconocimiento facial no está disponible en este equipo.");
        }
        Mat alineado = new Mat();
        Mat rasgo = new Mat();
        try {
            reconocedor.alignCrop(imagenBgr, cara, alineado);
            reconocedor.feature(alineado, rasgo);
            float[] vector = new float[LARGO_VECTOR];
            rasgo.get(0, 0, vector);
            return vector;
        } finally {
            alineado.release();
            rasgo.release();
        }
    }

    /** Similitud del coseno entre dos firmas faciales (de -1 a 1). */
    public static double similitud(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length || a.length == 0) return -1;
        double producto = 0;
        double normaA = 0;
        double normaB = 0;
        for (int i = 0; i < a.length; i++) {
            producto += (double) a[i] * b[i];
            normaA += (double) a[i] * a[i];
            normaB += (double) b[i] * b[i];
        }
        if (normaA == 0 || normaB == 0) return -1;
        return producto / (Math.sqrt(normaA) * Math.sqrt(normaB));
    }

    /** Recuadro (x, y, ancho, alto) de una detección, para dibujarlo en pantalla. */
    public static Rect recuadro(Mat cara) {
        if (cara == null) return null;
        // La fila de detección es CV_32F: se lee como float[] (con double[] falla).
        float[] datos = new float[15];
        cara.get(0, 0, datos);
        return new Rect((int) datos[0], (int) datos[1], (int) datos[2], (int) datos[3]);
    }

    /** Copia el modelo que viene dentro del proyecto a una carpeta temporal y devuelve su ruta. */
    private static String rutaModelo(String nombre) throws IOException {
        File carpeta = new File(System.getProperty("java.io.tmpdir"), "sigelab_modelos");
        if (!carpeta.isDirectory() && !carpeta.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta de modelos: " + carpeta);
        }
        File destino = new File(carpeta, nombre);
        if (destino.isFile() && destino.length() > 0) return destino.getAbsolutePath();
        try (InputStream recurso = ReconocimientoFacial.class.getResourceAsStream("/rostro/" + nombre)) {
            if (recurso == null) throw new IOException("No se encontró el recurso /rostro/" + nombre);
            Files.copy(recurso, destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return destino.getAbsolutePath();
    }
}
