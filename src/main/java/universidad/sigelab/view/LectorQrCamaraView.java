package universidad.sigelab.view;

import universidad.sigelab.controller.QrController;
import universidad.sigelab.util.BusinessException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Ventana que muestra lo que ve la cámara y espera hasta que aparece un código QR.
 * Solo devuelve el texto del QR: qué hacer con él lo decide la pantalla que la abrió.
 */
public class LectorQrCamaraView {

    private static final int PAUSA_ENTRE_CAPTURAS_MS = 100;

    // Componentes vinculados desde LectorQrCamaraView.form
    private JPanel panelPrincipal;

    private JComboBox<String> cboCamara;
    private JLabel lblVideo;
    private JLabel lblEstado;
    private JButton btnCancelar;

    // La View solamente conoce al Controller.
    private final QrController qrController;

    private JDialog ventana;

    /*
     * Cámara elegida en el ComboBox. Es volatile porque la escribe la
     * pantalla y la lee el hilo que captura las imágenes.
     */
    private volatile String camaraElegida;

    // Tarea en segundo plano que captura imágenes y busca el QR.
    private SwingWorker<String, BufferedImage> lector;

    // Resultado de la lectura: el código encontrado o el error que la detuvo.
    private String codigoLeido;
    private Exception error;


    public LectorQrCamaraView(QrController qrController) {
        this.qrController = qrController;
    }


    /*
     * Abre la ventana y no regresa hasta que se cierra.
     * Devuelve el texto del QR, o null si el usuario canceló.
     */
    public String leer(Component padre) {
        List<String> camaras = qrController.listarCamaras();
        if (camaras.isEmpty()) {
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

        // Al cerrarse la ventana, por cualquier motivo, se detiene la lectura y se apaga la cámara.
        ventana.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                // false: no se interrumpe al hilo a la mitad; termina su vuelta y apaga la cámara.
                lector.cancel(false);
            }
        });

        iniciarLectura();

        // La ventana es modal: esta línea espera aquí hasta que se cierre.
        ventana.setVisible(true);

        if (error instanceof RuntimeException problema) throw problema;
        if (error != null) throw new IllegalStateException(error);
        return codigoLeido;
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario(List<String> camaras) {
        for (String camara : camaras) cboCamara.addItem(camara);

        // Se empieza con la primera cámara detectada.
        camaraElegida = camaras.get(0);

        lblVideo.setHorizontalAlignment(SwingConstants.CENTER);
        lblVideo.setText("Encendiendo la cámara...");
        lblEstado.setText("Coloque el código QR frente a la cámara.");
    }

    private void configurarEventos() {
        // El hilo de lectura detecta el cambio y enciende la nueva cámara.
        cboCamara.addActionListener(e -> {
            camaraElegida = (String) cboCamara.getSelectedItem();
            lblVideo.setIcon(null);
            lblVideo.setText("Encendiendo la cámara...");
        });

        btnCancelar.addActionListener(e -> ventana.dispose());
    }


    /*
     * ========================================================
     * LECTURA EN SEGUNDO PLANO
     *
     * Capturar imágenes de la cámara es lento y continuo. Si se
     * hiciera en el hilo de la interfaz, la ventana se congelaría.
     * Por eso se usa un SwingWorker:
     *   doInBackground() captura y busca el QR en otro hilo,
     *   process() muestra cada imagen en la ventana,
     *   done() recibe el resultado y cierra la ventana.
     * ========================================================
     */
    private void iniciarLectura() {
        lector = new SwingWorker<>() {

            @Override
            protected String doInBackground() throws Exception {
                String camaraEncendida = null;
                try {
                    while (!isCancelled()) {

                        // La primera vez, o si el usuario eligió otra cámara, se enciende la elegida.
                        String elegida = camaraElegida;
                        if (!elegida.equals(camaraEncendida)) {
                            qrController.abrirCamara(elegida);
                            camaraEncendida = elegida;
                        }

                        BufferedImage imagen = qrController.capturarImagen();
                        if (imagen != null) {
                            publish(imagen);

                            // En cada imagen se busca un QR; la mayoría no tiene ninguno.
                            Optional<String> codigo = qrController.buscarCodigo(imagen);
                            if (codigo.isPresent()) return codigo.get();
                        }

                        Thread.sleep(PAUSA_ENTRE_CAPTURAS_MS);
                    }
                    return null;
                } finally {
                    // Pase lo que pase (QR leído, cancelación o error), la cámara se apaga.
                    qrController.cerrarCamara();
                }
            }

            @Override
            protected void process(List<BufferedImage> imagenes) {
                // Solo interesa la más reciente.
                lblVideo.setText(null);
                lblVideo.setIcon(new ImageIcon(imagenes.get(imagenes.size() - 1)));
            }

            @Override
            protected void done() {
                // Cancelado significa que el usuario cerró la ventana: no hay resultado.
                if (isCancelled()) return;

                try {
                    codigoLeido = get();
                } catch (ExecutionException e) {
                    error = e.getCause() instanceof Exception causa ? causa : e;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                ventana.dispose();
            }
        };

        lector.execute();
    }
}
