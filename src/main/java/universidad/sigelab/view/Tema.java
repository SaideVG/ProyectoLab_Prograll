package universidad.sigelab.view;

import java.awt.Color;
import java.awt.Font;

/**
 * Paleta y tipografías centralizadas.
 *
 * Todos los componentes visuales (login y registro de usuarios) toman sus
 * colores de aquí para que ambas pantallas se vean igual. La paleta sale de
 * la referencia de diseño: fondo claro con azul como color principal.
 */
final class Tema {

    private Tema() { }

    /** Segoe UI está en Windows; si no existe, Java usa una fuente por defecto parecida. */
    static final String FUENTE = "Segoe UI";

    // Fondos
    static final Color FONDO = new Color(0xF2F3F5);
    static final Color FONDO_CLARO = new Color(0xE8EEF9);
    static final Color SUPERFICIE = Color.WHITE;

    // Azul principal
    static final Color PRIMARIO = new Color(0x205FC9);
    static final Color PRIMARIO_HOVER = new Color(0x1B54B5);
    static final Color PRIMARIO_PRESIONADO = new Color(0x17479C);
    static final Color AZUL_CLARO = new Color(0xE0ECFF);
    static final Color AZUL_CLARO_HOVER = new Color(0xCFE0FF);

    // Textos y bordes
    static final Color TEXTO = new Color(0x1F2937);
    static final Color TEXTO_SUAVE = new Color(0x6B7280);
    static final Color BORDE = new Color(0xD0D7E1);

    // Estados
    static final Color PELIGRO = new Color(0xC0392B);
    static final Color PELIGRO_HOVER = new Color(0xA93226);
    static final Color PELIGRO_PRESIONADO = new Color(0x922B21);
    static final Color DESHABILITADO = new Color(0xC7CDD6);
    static final Color DESHABILITADO_TEXTO = new Color(0x8A9099);

    static Font titulo() { return new Font(FUENTE, Font.BOLD, 28); }
    static Font subtitulo() { return new Font(FUENTE, Font.PLAIN, 12); }
    static Font etiqueta() { return new Font(FUENTE, Font.BOLD, 12); }
    static Font texto() { return new Font(FUENTE, Font.PLAIN, 13); }
    static Font boton() { return new Font(FUENTE, Font.BOLD, 13); }
}
