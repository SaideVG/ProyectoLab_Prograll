package universidad.sigelab.util;

/** Validaciones de texto que repiten todos los Services. */
public final class Validador {
    private Validador() { }

    /** Devuelve el texto sin espacios sobrantes o rechaza la operación si viene vacío o es demasiado largo. */
    public static String requerido(String valor, String campo, int largoMaximo) {
        if (valor == null || valor.isBlank()) throw new BusinessException("El campo " + campo + " es obligatorio.");
        return opcional(valor, campo, largoMaximo);
    }

    /** Igual que requerido(), pero un texto vacío se guarda como null. */
    public static String opcional(String valor, String campo, int largoMaximo) {
        if (valor == null || valor.isBlank()) return null;
        String limpio = valor.trim();
        if (limpio.length() > largoMaximo) {
            throw new BusinessException("El campo " + campo + " admite máximo " + largoMaximo + " caracteres.");
        }
        return limpio;
    }
}
