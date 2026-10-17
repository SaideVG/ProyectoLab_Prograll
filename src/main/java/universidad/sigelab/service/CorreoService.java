package universidad.sigelab.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import universidad.sigelab.util.BusinessException;

import java.util.Properties;

/**
 * Envía correos (SMTP) con el código de recuperación de contraseña.
 *
 * La configuración se toma de variables de entorno o propiedades del sistema,
 * igual que la conexión a la base de datos; ninguna credencial queda en el
 * código. Si no hay SMTP configurado, {@link #configurado()} devuelve false y la
 * pantalla de recuperación muestra el código directamente, para que el flujo no
 * dependa de tener correo configurado durante la demostración.
 *
 * Variables de entorno:
 *   SIGELAB_SMTP_HOST      (obligatoria)
 *   SIGELAB_SMTP_PORT      (por defecto 587)
 *   SIGELAB_SMTP_USER      (obligatoria)
 *   SIGELAB_SMTP_PASSWORD  (obligatoria, suele ser una contraseña de aplicación)
 *   SIGELAB_SMTP_FROM      (por defecto, el mismo usuario)
 *   SIGELAB_SMTP_STARTTLS  (por defecto true; con el puerto 587)
 *   SIGELAB_SMTP_SSL       (por defecto false; activar con el puerto 465)
 */
public class CorreoService {

    private static final String HOST = configuracion("sigelab.smtp.host", "SIGELAB_SMTP_HOST", null);
    private static final String PORT = configuracion("sigelab.smtp.port", "SIGELAB_SMTP_PORT", "587");
    private static final String USER = configuracion("sigelab.smtp.user", "SIGELAB_SMTP_USER", null);
    private static final String PASSWORD = configuracion("sigelab.smtp.password", "SIGELAB_SMTP_PASSWORD", null);
    private static final String FROM = configuracion("sigelab.smtp.from", "SIGELAB_SMTP_FROM", null);
    private static final boolean STARTTLS = !"false".equalsIgnoreCase(
            configuracion("sigelab.smtp.starttls", "SIGELAB_SMTP_STARTTLS", "true"));
    private static final boolean SSL = "true".equalsIgnoreCase(
            configuracion("sigelab.smtp.ssl", "SIGELAB_SMTP_SSL", "false"));

    public boolean configurado() {
        return HOST != null && USER != null && PASSWORD != null;
    }

    public void enviar(String destino, String asunto, String cuerpo) {
        if (!configurado()) {
            throw new BusinessException("El envío de correo no está configurado.");
        }
        try {
            MimeMessage mensaje = new MimeMessage(sesion());
            mensaje.setFrom(new InternetAddress(FROM != null ? FROM : USER));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destino));
            mensaje.setSubject(asunto, "UTF-8");
            mensaje.setText(cuerpo, "UTF-8");
            Transport.send(mensaje);
        } catch (Exception e) {
            throw new BusinessException("No se pudo enviar el correo. Revise la configuración SMTP.", e);
        }
    }

    private Session sesion() {
        return Session.getInstance(propiedades(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USER, PASSWORD);
            }
        });
    }

    private Properties propiedades() {
        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.host", HOST);
        propiedades.put("mail.smtp.port", PORT);
        propiedades.put("mail.smtp.auth", "true");
        propiedades.put("mail.smtp.starttls.enable", String.valueOf(STARTTLS));
        propiedades.put("mail.smtp.ssl.enable", String.valueOf(SSL));
        // Tiempos de espera cortos: si el servidor no responde, se cae rápido al respaldo en pantalla.
        propiedades.put("mail.smtp.connectiontimeout", "8000");
        propiedades.put("mail.smtp.timeout", "8000");
        propiedades.put("mail.smtp.writetimeout", "8000");
        return propiedades;
    }

    private static String configuracion(String propiedad, String variable, String valorPorDefecto) {
        String valor = System.getProperty(propiedad);
        if (valor == null || valor.isBlank()) valor = System.getenv(variable);
        return (valor == null || valor.isBlank()) ? valorPorDefecto : valor.trim();
    }
}
