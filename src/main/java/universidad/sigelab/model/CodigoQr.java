package universidad.sigelab.model;

import java.awt.image.BufferedImage;

/** Imagen QR de un equipo. El QR solo contiene su código de inventario. */
public record CodigoQr(String codigoInventario, BufferedImage imagen) { }
