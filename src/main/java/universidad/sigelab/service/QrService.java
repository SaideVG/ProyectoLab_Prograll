package universidad.sigelab.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import universidad.sigelab.enums.Modulo;
import universidad.sigelab.model.CodigoQr;
import universidad.sigelab.model.Equipo;
import universidad.sigelab.repository.EquipoRepository;
import universidad.sigelab.util.BusinessException;
import universidad.sigelab.util.Sesion;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Genera y lee códigos QR con la librería ZXing.
 * El QR tiene una sola finalidad: identificar un equipo por su código de inventario.
 */
public class QrService {
    private static final int TAMANO_EN_PIXELES = 300;
    private static final int LARGO_MAXIMO_CODIGO = 30;
    private static final File CARPETA_DE_IMAGENES = new File("qr");

    private final EquipoRepository equipoRepository;
    public QrService() { this(new EquipoRepository()); }
    public QrService(EquipoRepository equipoRepository) { this.equipoRepository = equipoRepository; }

    /**
     * Genera el QR de un equipo registrado. Dentro del QR solo va su código de inventario:
     * el estado y los demás datos no se guardan en la imagen, siempre se consultan en la base.
     */
    public CodigoQr generarParaEquipo(int idEquipo) {
        Sesion.exigirAcceso(Modulo.EQUIPOS);
        Equipo equipo = equipoRepository.buscarPorId(idEquipo)
                .orElseThrow(() -> new BusinessException("El equipo ya no existe."));
        String codigo = equipo.getCodigoInventario();
        try {
            BitMatrix matriz = new QRCodeWriter().encode(codigo, BarcodeFormat.QR_CODE,
                    TAMANO_EN_PIXELES, TAMANO_EN_PIXELES);
            return new CodigoQr(codigo, MatrixToImageWriter.toBufferedImage(matriz));
        } catch (WriterException e) {
            throw new BusinessException("No se pudo generar el código QR del equipo " + codigo + ".", e);
        }
    }

    /** Guarda el QR como imagen PNG y devuelve el archivo creado. */
    public File guardar(CodigoQr qr, File destino) {
        Sesion.exigirAcceso(Modulo.EQUIPOS);
        File archivo = destino.getName().toLowerCase().endsWith(".png") ? destino : new File(destino.getPath() + ".png");
        try {
            ImageIO.write(qr.imagen(), "png", archivo);
            return archivo;
        } catch (IOException e) {
            throw new BusinessException("No se pudo guardar la imagen en " + archivo.getPath() + ".", e);
        }
    }

    /**
     * Lee el texto de un QR a partir de un archivo de imagen y lo devuelve.
     * RN-19: la lectura solo identifica. Ni este método ni buscarCodigo() consultan o modifican
     * ningún equipo; con el código devuelto, PrestamoService busca el equipo real y decide si puede prestarse.
     */
    public String leerCodigo(File archivoDeImagen) {
        Sesion.exigirAcceso(Modulo.PRESTAMOS);
        return buscarCodigo(cargarImagen(archivoDeImagen))
                .orElseThrow(() -> new BusinessException("No se encontró ningún código QR en la imagen seleccionada."));
    }

    /**
     * Busca un QR dentro de una imagen ya cargada: un archivo o un cuadro capturado por la cámara.
     * Si la imagen no contiene ningún QR devuelve vacío (con la cámara es lo normal en casi
     * todos los cuadros, hasta que el código queda frente al lente).
     */
    public Optional<String> buscarCodigo(BufferedImage imagen) {
        Sesion.exigirAcceso(Modulo.PRESTAMOS);
        try {
            BinaryBitmap mapaDeBits = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(imagen)));
            Map<DecodeHintType, Object> opciones = Map.of(
                    DecodeHintType.POSSIBLE_FORMATS, List.of(BarcodeFormat.QR_CODE),
                    DecodeHintType.TRY_HARDER, Boolean.TRUE);

            String texto = new MultiFormatReader().decode(mapaDeBits, opciones).getText().trim();

            // Un QR ajeno al sistema (un enlace, por ejemplo) no puede ser un código de inventario.
            if (texto.isEmpty() || texto.length() > LARGO_MAXIMO_CODIGO) {
                throw new BusinessException("El QR leído no contiene un código de inventario.");
            }
            return Optional.of(texto);
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    /** Carpeta donde se proponen guardar y buscar las imágenes QR. Se crea la primera vez que se usa. */
    public File carpetaDeImagenes() {
        CARPETA_DE_IMAGENES.mkdirs();
        return CARPETA_DE_IMAGENES.getAbsoluteFile();
    }

    private BufferedImage cargarImagen(File archivo) {
        if (archivo == null || !archivo.isFile()) throw new BusinessException("Seleccione un archivo de imagen.");
        try {
            BufferedImage imagen = ImageIO.read(archivo);
            if (imagen == null) throw new BusinessException("El archivo " + archivo.getName() + " no es una imagen válida.");
            return imagen;
        } catch (IOException e) {
            throw new BusinessException("No se pudo abrir el archivo " + archivo.getName() + ".", e);
        }
    }
}
