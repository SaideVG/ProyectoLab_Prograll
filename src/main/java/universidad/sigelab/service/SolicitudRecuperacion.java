package universidad.sigelab.service;

/**
 * Resultado de pedir un código de recuperación de contraseña.
 *
 * Si el correo está configurado, {@code enviadoPorCorreo} es true y el código
 * viaja por correo. Si no lo está (o el envío falló), {@code codigoEnPantalla}
 * trae el código para mostrarlo en pantalla y no dejar al usuario sin salida.
 */
public class SolicitudRecuperacion {

    private final boolean enviadoPorCorreo;
    private final String codigoEnPantalla;
    private final String correoEnmascarado;

    public SolicitudRecuperacion(boolean enviadoPorCorreo, String codigoEnPantalla, String correoEnmascarado) {
        this.enviadoPorCorreo = enviadoPorCorreo;
        this.codigoEnPantalla = codigoEnPantalla;
        this.correoEnmascarado = correoEnmascarado;
    }

    public boolean isEnviadoPorCorreo() { return enviadoPorCorreo; }

    /** Código a mostrar en pantalla, o null cuando se envió por correo. */
    public String getCodigoEnPantalla() { return codigoEnPantalla; }

    /** Correo de la cuenta con el usuario oculto (ej. j***@universidad.edu). */
    public String getCorreoEnmascarado() { return correoEnmascarado; }
}
