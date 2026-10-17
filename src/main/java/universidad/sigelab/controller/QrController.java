package universidad.sigelab.controller;

import universidad.sigelab.model.CodigoQr;
import universidad.sigelab.service.CamaraService;
import universidad.sigelab.service.QrService;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Optional;

public class QrController {
    private final QrService service;
    private final CamaraService camaraService;
    public QrController() { this(new QrService(), new CamaraService()); }
    public QrController(QrService service, CamaraService camaraService) {
        this.service = service;
        this.camaraService = camaraService;
    }

    // Generar
    public CodigoQr generarParaEquipo(int idEquipo) { return service.generarParaEquipo(idEquipo); }
    public File guardar(CodigoQr qr, File destino) { return service.guardar(qr, destino); }
    public File carpetaDeImagenes() { return service.carpetaDeImagenes(); }

    // Leer desde un archivo de imagen
    public String leerCodigo(File archivoDeImagen) { return service.leerCodigo(archivoDeImagen); }

    // Leer con la cámara: se captura una imagen tras otra y en cada una se busca un QR
    public List<String> listarCamaras() { return camaraService.listarCamaras(); }
    public void abrirCamara(String nombreDeCamara) { camaraService.abrir(nombreDeCamara); }
    public BufferedImage capturarImagen() { return camaraService.capturar(); }
    public void cerrarCamara() { camaraService.cerrar(); }
    public Optional<String> buscarCodigo(BufferedImage imagen) { return service.buscarCodigo(imagen); }
}
