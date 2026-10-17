package universidad.sigelab.service;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamResolution;
import universidad.sigelab.util.BusinessException;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Acceso a la cámara web con la librería Webcam Capture.
 * Es la única clase del proyecto que conoce esa librería: el resto solo pide imágenes.
 */
public class CamaraService {
    private static final Dimension RESOLUCION = WebcamResolution.VGA.getSize();
    private static final long ESPERA_DE_DETECCION_MS = 8000;
    private static final int ESPERA_DE_ENCENDIDO_SEG = 10;

    // Cámara que está encendida en este momento; null si no hay ninguna.
    private Webcam camaraAbierta;

    /*
     * Queda en true si una cámara nunca respondió al encenderla. La librería atiende las
     * cámaras de una en una, así que desde ese momento no puede usar ninguna hasta que
     * se cierre y se vuelva a abrir el sistema.
     */
    private boolean sinRespuesta;

    /**
     * Nombres de las cámaras conectadas. Solo las enumera: no enciende ninguna.
     * Se descartan las cámaras virtuales (la de OBS, por ejemplo): no pueden ver un QR
     * impreso y no entregan la imagen en el tamaño que se les pide, así que nunca responden.
     */
    public List<String> listarCamaras() {
        try {
            return Webcam.getWebcams(ESPERA_DE_DETECCION_MS).stream()
                    .map(Webcam::getName)
                    .filter(nombre -> !nombre.toLowerCase().contains("virtual"))
                    .toList();
        } catch (TimeoutException | WebcamException e) {
            throw new BusinessException("No se pudieron detectar las cámaras de este equipo.", e);
        }
    }

    /** Enciende la cámara indicada. Si había otra encendida, primero la apaga. */
    public synchronized void abrir(String nombreDeCamara) {
        if (sinRespuesta) {
            throw new BusinessException("La cámara dejó de responder. Cierre el sistema y ábralo de nuevo " +
                    "para volver a usarla; mientras tanto puede leer el QR desde una imagen.");
        }
        cerrar();

        Webcam camara = Webcam.getWebcams().stream()
                .filter(candidata -> candidata.getName().equals(nombreDeCamara))
                .findFirst()
                .orElseThrow(() -> new BusinessException("La cámara " + nombreDeCamara + " ya no está conectada."));
        String mensajeNoAbre = "No se pudo abrir la cámara " + nombreDeCamara +
                ". Verifique que no la esté usando otro programa.";

        /*
         * Encender la cámara se hace en un hilo aparte para poder ponerle un límite de tiempo:
         * si la cámara nunca responde, el sistema deja de esperar en lugar de quedarse congelado.
         */
        ExecutorService ejecutor = Executors.newSingleThreadExecutor(tarea -> {
            Thread hilo = new Thread(tarea, "encender-camara");
            hilo.setDaemon(true);
            return hilo;
        });
        Future<Boolean> encendido = ejecutor.submit(() -> {
            if (camara.isOpen()) camara.close();
            camara.setViewSize(RESOLUCION);
            return camara.open();
        });

        try {
            if (!encendido.get(ESPERA_DE_ENCENDIDO_SEG, TimeUnit.SECONDS)) throw new BusinessException(mensajeNoAbre);
            camaraAbierta = camara;
        } catch (TimeoutException e) {
            encendido.cancel(true);
            sinRespuesta = true;
            throw new BusinessException("La cámara " + nombreDeCamara + " no respondió. Cierre el sistema y ábralo " +
                    "de nuevo; mientras tanto puede leer el QR desde una imagen.", e);
        } catch (ExecutionException e) {
            throw new BusinessException(mensajeNoAbre, e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(mensajeNoAbre, e);
        } finally {
            ejecutor.shutdown();
        }
    }

    /** Imagen que la cámara ve en este instante; null si no hay cámara encendida. */
    public synchronized BufferedImage capturar() {
        return camaraAbierta == null ? null : camaraAbierta.getImage();
    }

    /** Apaga la cámara. Se puede llamar aunque ya esté apagada. */
    public synchronized void cerrar() {
        if (camaraAbierta != null) {
            camaraAbierta.close();
            camaraAbierta = null;
        }
    }
}
