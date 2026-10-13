package universidad.sigelab.view;

import java.awt.Color;
import java.awt.Font;

/**
 * Paleta y tipografías centralizadas.
 *
 * Los colores salen de la referencia de diseño (Stitch): azul principal
 * #1D5BD8 sobre fondos muy claros, con textos gris azulado.
 */
final class Tema {

    private Tema() { }

    /** Segoe UI está en Windows; si no existe, Java usa una fuente por defecto parecida. */
    static final String FUENTE = "Segoe UI";

    // Fondos
    static final Color FONDO = new Color(0xF4F8FB);
    static final Color PANEL = Color.WHITE;
    static final Color SUPERFICIE = Color.WHITE;
    static final Color SUPERFICIE_ALT = new Color(0xF8FAFD);

    // Azul principal (tomado de la referencia #1D5BD8)
    static final Color PRIMARIO = new Color(0x1D5BD8);
    static final Color PRIMARIO_HOVER = new Color(0x1849B4);
    static final Color PRIMARIO_PRESIONADO = new Color(0x143C93);
    static final Color AZUL_CLARO = new Color(0xE7EFFC);
    static final Color AZUL_CLARO_HOVER = new Color(0xD6E4FA);

    // Textos y bordes
    static final Color TEXTO = new Color(0x1F2937);
    static final Color ETIQUETA = new Color(0x334155);
    static final Color TEXTO_SUAVE = new Color(0x6B7280);
    static final Color PLACEHOLDER = new Color(0x94A3B8);
    static final Color PIE = new Color(0x94A3B8);
    static final Color BORDE = new Color(0xDCE3EC);
    static final Color BORDE_CAMPO = new Color(0xD7DEE8);

    // Estados
    static final Color PELIGRO = new Color(0xC0392B);
    static final Color PELIGRO_HOVER = new Color(0xA93226);
    static final Color PELIGRO_PRESIONADO = new Color(0x922B21);
    static final Color EXITO = new Color(0x22C55E);
    static final Color DESHABILITADO = new Color(0xC7CDD6);
    static final Color DESHABILITADO_TEXTO = new Color(0x8A9099);

    static Font titulo() { return new Font(FUENTE, Font.BOLD, 30); }
    static Font subtitulo() { return new Font(FUENTE, Font.PLAIN, 13); }
    static Font etiqueta() { return new Font(FUENTE, Font.BOLD, 11); }
    static Font etiquetaMedia() { return new Font(FUENTE, Font.BOLD, 12); }
    static Font texto() { return new Font(FUENTE, Font.PLAIN, 14); }
    static Font boton() { return new Font(FUENTE, Font.BOLD, 13); }
    static Font pie() { return new Font(FUENTE, Font.PLAIN, 11); }
    static Font enlace() { return new Font(FUENTE, Font.PLAIN, 12); }
}
