package universidad.sigelab.service;

import org.mindrot.jbcrypt.BCrypt;
import universidad.sigelab.enums.AccionAuditoria;
import universidad.sigelab.enums.EstadoRegistro;
import universidad.sigelab.enums.Modulo;
import universidad.sigelab.model.Rol;
import universidad.sigelab.model.Usuario;
import universidad.sigelab.repository.RolRepository;
import universidad.sigelab.repository.UsuarioRepository;
import universidad.sigelab.util.BusinessException;
import universidad.sigelab.util.Sesion;
import universidad.sigelab.util.Validador;

import java.util.List;
import java.util.Optional;

public class UsuarioService {
    private static final String ENTIDAD = "Usuario";
    private static final String CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos.";
    private static final int LARGO_MINIMO_PASSWORD = 6;

    /**
     * USUARIO DE PRUEBA QUEMADO EN CÓDIGO (solo para desarrollo): admin / admin.
     * Con estas credenciales se entra sin comparar contra el hash guardado en la base.
     * Lo demás no cambia: el usuario "admin" debe existir en la tabla Usuario y estar ACTIVO,
     * así la sesión tiene un ID real para la auditoría y los préstamos.
     * Eliminar estas constantes y esUsuarioDePrueba() antes de la entrega final.
     */
    private static final String USUARIO_PRUEBA = "admin";
    private static final String PASSWORD_PRUEBA = "admin";

    private final UsuarioRepository repository;
    private final RolRepository rolRepository;
    private final AuditoriaService auditoriaService;
    public UsuarioService() { this(new UsuarioRepository(), new RolRepository(), new AuditoriaService()); }
    public UsuarioService(UsuarioRepository repository, RolRepository rolRepository, AuditoriaService auditoriaService) {
        this.repository = repository;
        this.rolRepository = rolRepository;
        this.auditoriaService = auditoriaService;
    }

    /*
     * ========================================================
     * INICIO DE SESIÓN
     * ========================================================
     */

    /**
     * Primer factor del login: usuario, contraseña y estado.
     * La validación facial (RN-02) todavía no existe; cuando se integre debe aprobarse
     * después de este método y antes de considerar el login como exitoso.
     */
    public Usuario autenticar(String nombreUsuario, String password) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) throw new BusinessException("Ingrese su usuario.");
        if (password == null || password.isEmpty()) throw new BusinessException("Ingrese su contraseña.");

        Optional<Usuario> encontrado = repository.buscarPorUsuario(nombreUsuario.trim());
        if (encontrado.isEmpty()) {
            auditoriaService.registrar(null, AccionAuditoria.LOGIN_FALLIDO, ENTIDAD, null,
                    "Usuario inexistente: " + nombreUsuario.trim());
            throw new BusinessException(CREDENCIALES_INVALIDAS);
        }

        Usuario usuario = encontrado.get();
        if (!esUsuarioDePrueba(usuario, password) && !passwordCorrecta(password, usuario.getPasswordHash())) {
            registrarLoginFallido(usuario, "Contraseña incorrecta.");
            throw new BusinessException(CREDENCIALES_INVALIDAS);
        }

        // RN-01. Se revisa después de la contraseña para no revelar el estado de una cuenta a quien no la conoce.
        if (!usuario.isActivo()) {
            registrarLoginFallido(usuario, "Usuario inactivo.");
            throw new BusinessException("El usuario está INACTIVO. Contacte al administrador.");
        }

        auditoriaService.registrar(usuario, AccionAuditoria.LOGIN_EXITOSO, ENTIDAD, usuario.getIdUsuario(),
                "Inicio de sesión.");
        return usuario;
    }

    /*
     * ========================================================
     * ADMINISTRACIÓN DE USUARIOS
     * ========================================================
     */

    public boolean guardar(Usuario usuario, String password) {
        Sesion.exigirAcceso(Modulo.USUARIOS);
        validar(usuario);
        exigirDatosUnicos(usuario, 0);
        usuario.setPasswordHash(BCrypt.hashpw(passwordValida(password), BCrypt.gensalt()));
        usuario.setEstado(EstadoRegistro.ACTIVO);
        return repository.guardar(usuario);
    }

    /** Si password llega vacío, el usuario conserva la contraseña que ya tenía. */
    public boolean actualizar(Usuario usuario, String password) {
        Sesion.exigirAcceso(Modulo.USUARIOS);
        validar(usuario);
        Usuario actual = repository.buscarPorId(usuario.getIdUsuario())
                .orElseThrow(() -> new BusinessException("El usuario ya no existe."));
        exigirDatosUnicos(usuario, usuario.getIdUsuario());
        boolean cambiaPassword = password != null && !password.isEmpty();
        usuario.setPasswordHash(cambiaPassword
                ? BCrypt.hashpw(passwordValida(password), BCrypt.gensalt())
                : actual.getPasswordHash());
        // El estado solo cambia con cambiarEstado().
        usuario.setEstado(actual.getEstado());
        return repository.actualizar(usuario);
    }

    /** Desactivación lógica: el usuario no se borra, para conservar su historial. */
    public boolean cambiarEstado(int idUsuario, EstadoRegistro nuevoEstado) {
        Sesion.exigirAcceso(Modulo.USUARIOS);
        Usuario usuario = repository.buscarPorId(idUsuario)
                .orElseThrow(() -> new BusinessException("El usuario ya no existe."));
        Usuario quienOpera = Sesion.getUsuarioActual();
        if (nuevoEstado == EstadoRegistro.INACTIVO && idUsuario == quienOpera.getIdUsuario()) {
            throw new BusinessException("No puede desactivar su propio usuario.");
        }
        usuario.setEstado(nuevoEstado);
        boolean actualizado = repository.actualizar(usuario);
        if (actualizado && nuevoEstado == EstadoRegistro.INACTIVO) {
            auditoriaService.registrar(quienOpera, AccionAuditoria.USUARIO_DESACTIVADO, ENTIDAD, idUsuario,
                    "Usuario desactivado: " + usuario.getUsuario());
        }
        return actualizado;
    }

    public List<Usuario> listar() {
        Sesion.exigirAcceso(Modulo.USUARIOS);
        return repository.listar();
    }

    public Optional<Usuario> buscar(int id) {
        Sesion.exigirAcceso(Modulo.USUARIOS);
        return repository.buscarPorId(id);
    }

    public Optional<Usuario> buscarPorUsuario(String nombreUsuario) {
        Sesion.exigirAcceso(Modulo.USUARIOS);
        return repository.buscarPorUsuario(nombreUsuario);
    }

    public List<Rol> listarRoles() {
        Sesion.exigirAcceso(Modulo.USUARIOS);
        return rolRepository.listar();
    }

    private void validar(Usuario usuario) {
        if (usuario == null) throw new BusinessException("El usuario es obligatorio.");
        usuario.setNombre(Validador.requerido(usuario.getNombre(), "Nombre", 100));
        usuario.setApellido(Validador.requerido(usuario.getApellido(), "Apellido", 100));
        usuario.setUsuario(Validador.requerido(usuario.getUsuario(), "Usuario", 50));
        usuario.setCorreo(Validador.requerido(usuario.getCorreo(), "Correo", 150));
        if (usuario.getUsuario().contains(" ")) throw new BusinessException("El nombre de usuario no puede tener espacios.");
        if (!usuario.getCorreo().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new BusinessException("El correo no tiene un formato válido.");
        }
        if (usuario.getRol() == null || rolRepository.buscarPorId(usuario.getRol().getIdRol()).isEmpty()) {
            throw new BusinessException("Seleccione un rol válido.");
        }
    }

    private void exigirDatosUnicos(Usuario usuario, int idExcluido) {
        if (repository.existeUsuario(usuario.getUsuario(), idExcluido)) {
            throw new BusinessException("Ya existe un usuario con el nombre " + usuario.getUsuario() + ".");
        }
        if (repository.existeCorreo(usuario.getCorreo(), idExcluido)) {
            throw new BusinessException("Ya existe un usuario con el correo " + usuario.getCorreo() + ".");
        }
    }

    private String passwordValida(String password) {
        if (password == null || password.length() < LARGO_MINIMO_PASSWORD) {
            throw new BusinessException("La contraseña debe tener al menos " + LARGO_MINIMO_PASSWORD + " caracteres.");
        }
        return password;
    }

    private void registrarLoginFallido(Usuario usuario, String motivo) {
        auditoriaService.registrar(usuario, AccionAuditoria.LOGIN_FALLIDO, ENTIDAD, usuario.getIdUsuario(), motivo);
    }

    private boolean esUsuarioDePrueba(Usuario usuario, String password) {
        return USUARIO_PRUEBA.equals(usuario.getUsuario()) && PASSWORD_PRUEBA.equals(password);
    }

    private boolean passwordCorrecta(String password, String hash) {
        try { return BCrypt.checkpw(password, hash); }
        catch (IllegalArgumentException e) { return false; }
    }
}
