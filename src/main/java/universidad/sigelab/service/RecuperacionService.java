package universidad.sigelab.service;

import org.mindrot.jbcrypt.BCrypt;
import universidad.sigelab.enums.AccionAuditoria;
import universidad.sigelab.model.Usuario;
import universidad.sigelab.repository.TokenRecuperacionRepository;
import universidad.sigelab.repository.UsuarioRepository;
import universidad.sigelab.util.BusinessException;
import universidad.sigelab.util.JdbcSupport;
import universidad.sigelab.util.Transaccion;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Optional;

/**
 * Recuperación de contraseña con código de un solo uso.
 *
 * Es funcionalidad adicional a las reglas del PDF: el login normal sigue siendo
 * usuario + contraseña + rostro (RN-02). El código se guarda en la base como
 * hash SHA-256, vence a los pocos minutos y solo sirve una vez. Cuando el correo
 * no está configurado (o el envío falla) el código se devuelve para mostrarlo en
 * pantalla, de modo que el flujo nunca deja al usuario bloqueado.
 */
public class RecuperacionService {

    private static final String ENTIDAD = "Usuario";
    private static final int LARGO_CODIGO = 6;
    private static final int MINUTOS_VIGENCIA = 10;
    private static final int LARGO_MINIMO_PASSWORD = 6;

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacionRepository tokenRepository;
    private final CorreoService correoService;
    private final AuditoriaService auditoriaService;
    private final SecureRandom aleatorio = new SecureRandom();

    public RecuperacionService() {
        this(new UsuarioRepository(), new TokenRecuperacionRepository(), new CorreoService(), new AuditoriaService());
    }

    public RecuperacionService(UsuarioRepository usuarioRepository, TokenRecuperacionRepository tokenRepository,
                               CorreoService correoService, AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.correoService = correoService;
        this.auditoriaService = auditoriaService;
    }

    public boolean correoConfigurado() {
        return correoService.configurado();
    }

    /** Genera un código para la cuenta y lo envía por correo (o lo devuelve para mostrarlo en pantalla). */
    public SolicitudRecuperacion solicitar(String usuarioOCorreo) {
        Usuario usuario = buscarCuenta(usuarioOCorreo);
        if (!usuario.isActivo()) {
            throw new BusinessException("La cuenta está INACTIVA. Contacte al administrador.");
        }

        String codigo = generarCodigo();
        tokenRepository.invalidarPendientes(usuario.getIdUsuario());
        tokenRepository.crear(usuario.getIdUsuario(), hash(codigo), MINUTOS_VIGENCIA);

        auditoriaService.registrar(usuario, AccionAuditoria.RECUPERACION_SOLICITADA, ENTIDAD,
                usuario.getIdUsuario(), "Solicitud de recuperación de contraseña.");

        String correo = enmascarar(usuario.getCorreo());
        if (correoService.configurado()) {
            try {
                correoService.enviar(usuario.getCorreo(), "SIGELAB - Código de recuperación de contraseña",
                        cuerpo(codigo));
                return new SolicitudRecuperacion(true, null, correo);
            } catch (RuntimeException e) {
                throw new BusinessException("No se pudo enviar el correo electrónico. Revise la configuración SMTP.", e);
            }
        }
        throw new BusinessException("El servidor de correo no está configurado. Configure SIGELAB_SMTP_* o solicite apoyo al administrador.");
    }

    /** Verifica el código y, si es válido, guarda la nueva contraseña en una sola transacción. */
    public void restablecer(String usuarioOCorreo, String codigo, String nuevaPassword) {
        validarPassword(nuevaPassword);
        Usuario usuario = buscarCuenta(usuarioOCorreo);
        byte[] hash = hash(codigo == null ? "" : codigo.trim());
        String passwordHash = BCrypt.hashpw(nuevaPassword, BCrypt.gensalt());

        try {
            Transaccion.ejecutar("restablecer la contraseña", connection -> {
                if (!tokenRepository.consumir(connection, usuario.getIdUsuario(), hash)) {
                    throw new BusinessException("El código es incorrecto o ya venció. Solicite uno nuevo.");
                }
                JdbcSupport.actualizar(connection, "UPDATE Usuario SET password_hash = ? WHERE id_usuario = ?",
                        passwordHash, usuario.getIdUsuario());
                auditoriaService.registrar(connection, usuario, AccionAuditoria.PASSWORD_RESTABLECIDA, ENTIDAD,
                        usuario.getIdUsuario(), "Contraseña restablecida con código de recuperación.");
                return null;
            });
        } catch (BusinessException e) {
            // El intento fallido se audita fuera de la transacción (que ya se deshizo).
            auditoriaService.registrar(usuario, AccionAuditoria.RECUPERACION_FALLIDA, ENTIDAD,
                    usuario.getIdUsuario(), "Código de recuperación incorrecto o vencido.");
            throw e;
        }
    }

    private Usuario buscarCuenta(String usuarioOCorreo) {
        String dato = usuarioOCorreo == null ? "" : usuarioOCorreo.trim();
        if (dato.isEmpty()) throw new BusinessException("Ingrese su usuario o correo.");
        Optional<Usuario> encontrado = usuarioRepository.buscarPorUsuario(dato);
        if (encontrado.isEmpty()) encontrado = usuarioRepository.buscarPorCorreo(dato);
        return encontrado.orElseThrow(() -> new BusinessException("No existe una cuenta con ese usuario o correo."));
    }

    private void validarPassword(String password) {
        if (password == null || password.length() < LARGO_MINIMO_PASSWORD) {
            throw new BusinessException("La contraseña debe tener al menos " + LARGO_MINIMO_PASSWORD + " caracteres.");
        }
    }

    private String generarCodigo() {
        StringBuilder codigo = new StringBuilder(LARGO_CODIGO);
        for (int i = 0; i < LARGO_CODIGO; i++) codigo.append(aleatorio.nextInt(10));
        return codigo.toString();
    }

    private byte[] hash(String codigo) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(codigo.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new BusinessException("No se pudo generar el código de recuperación.", e);
        }
    }

    private String enmascarar(String correo) {
        int arroba = correo.indexOf('@');
        if (arroba <= 1) return correo;
        return correo.charAt(0) + "***" + correo.substring(arroba);
    }

    private String cuerpo(String codigo) {
        return "SIGELAB - Recuperación de contraseña\n\n"
                + "Su código de verificación es: " + codigo + "\n\n"
                + "El código vence en " + MINUTOS_VIGENCIA + " minutos y solo puede usarse una vez.\n"
                + "Si usted no solicitó este cambio, ignore este mensaje.\n";
    }
}
