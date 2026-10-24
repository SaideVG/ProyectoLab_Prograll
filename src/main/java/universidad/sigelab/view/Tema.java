package universidad.sigelab.view;

import java.awt.Color;
import java.awt.Font;

/**
 * Paleta y tipografías centralizadas para MODO OSCURO (Dark Theme).
 */
final class Tema {

    private Tema() { }

    static final String FUENTE = "Segoe UI";

    // Fondos oscuros de la interfaz
    static final Color FONDO = new Color(0x06, 0x0B, 0x14);             // Fondo general profundo
    static final Color PANEL = new Color(0x09, 0x11, 0x22);             // Paneles contenedores / inputs
    static final Color SUPERFICIE = new Color(0x0B, 0x12, 0x22);        // Superficie principal
    static final Color SUPERFICIE_ALT = new Color(0x0C, 0x16, 0x2C);    // Tarjeta derecha

    // Azul Neón / Cian (Colores de la imagen)
    static final Color PRIMARIO = new Color(0x00, 0xC9, 0xFF);          // Botón principal cian brillante
    static final Color PRIMARIO_HOVER = new Color(0x33, 0xD4, 0xFF);    // Hover cian brillante
    static final Color PRIMARIO_PRESIONADO = new Color(0x00, 0xA3, 0xCC);
    static final Color AZUL_CLARO = new Color(0x13, 0x26, 0x40);        // Fondo botones secundarios
    static final Color AZUL_CLARO_HOVER = new Color(0x1E, 0x3A, 0x5F);

    // Textos y bordes
    static final Color TEXTO = new Color(0xFF, 0xFF, 0xFF);             // Texto blanco/brillante
    static final Color ETIQUETA = new Color(0x38, 0xBD, 0xF8);          // Títulos pequeños y acentos cian
    static final Color TEXTO_SUAVE = new Color(0x8E, 0x9B, 0xB0);       // Subtítulos
    static final Color PLACEHOLDER = new Color(0x47, 0x55, 0x69);       // Placeholder tenue
    static final Color PIE = new Color(0x64, 0x74, 0x8B);               // Pie de página
    static final Color BORDE = new Color(0x13, 0x26, 0x40);             // Bordes oscuros
    static final Color BORDE_CAMPO = new Color(0x00, 0xC9, 0xFF);       // Borde cian para inputs

    // Estados
    static final Color PELIGRO = new Color(0xEF, 0x44, 0x44);
    static final Color PELIGRO_HOVER = new Color(0xDC, 0x26, 0x26);
    static final Color PELIGRO_PRESIONADO = new Color(0xB9, 0x1C, 0x1C);
    static final Color EXITO = new Color(0x10, 0xB9, 0x81);
    static final Color DESHABILITADO = new Color(0x1E, 0x29, 0x3B);
    static final Color DESHABILITADO_TEXTO = new Color(0x47, 0x55, 0x69);

    static Font titulo() { return new Font(FUENTE, Font.BOLD, 26); }
    static Font subtitulo() { return new Font(FUENTE, Font.PLAIN, 12); }
    static Font etiqueta() { return new Font(FUENTE, Font.BOLD, 11); }
    static Font etiquetaMedia() { return new Font(FUENTE, Font.BOLD, 12); }
    static Font texto() { return new Font(FUENTE, Font.PLAIN, 13); }
    static Font boton() { return new Font(FUENTE, Font.BOLD, 14); }
    static Font pie() { return new Font(FUENTE, Font.PLAIN, 11); }
    static Font enlace() { return new Font(FUENTE, Font.PLAIN, 12); }
}