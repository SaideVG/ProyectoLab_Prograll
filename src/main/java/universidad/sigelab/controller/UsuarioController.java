package universidad.sigelab.controller;

import universidad.sigelab.enums.EstadoRegistro;
import universidad.sigelab.model.Rol;
import universidad.sigelab.model.Usuario;
import universidad.sigelab.service.RecuperacionService;
import universidad.sigelab.service.SolicitudRecuperacion;
import universidad.sigelab.service.UsuarioService;
import universidad.sigelab.service.ValidadorRostro;
import universidad.sigelab.util.Sesion;

import java.util.List;
import java.util.Optional;

public class UsuarioController {
    private final UsuarioService service;
    private final RecuperacionService recuperacionService;

    public UsuarioController() { this(new UsuarioService(), new RecuperacionService()); }
    public UsuarioController(UsuarioService service) { this(service, new RecuperacionService()); }

    public UsuarioController(UsuarioService service, RecuperacionService recuperacionService) {
        this.service = service;
        this.recuperacionService = recuperacionService;
    }

    /**
     * Inicia sesión con los dos factores. El {@link ValidadorRostro} lo aporta la
     * View (pantalla de la cámara); puede ser null cuando el usuario no tiene
     * rostro registrado.
     */
    public Usuario iniciarSesion(String usuario, String password, ValidadorRostro validadorRostro) {
        Usuario autenticado = service.autenticar(usuario, password, validadorRostro);
        Sesion.iniciar(autenticado);
        return autenticado;
    }

    public boolean tieneRostroRegistrado(int idUsuario) { return service.tieneRostroRegistrado(idUsuario); }
    public void registrarRostro(Usuario usuario, float[] vector) { service.registrarRostro(usuario, vector); }
    public void eliminarRostro(Usuario usuario) { service.eliminarRostro(usuario); }

    public void cerrarSesion() { Sesion.cerrar(); }

    /* ========================================================
       RECUPERACIÓN DE CONTRASEÑA (código de un solo uso)
       ======================================================== */

    public boolean correoConfigurado() { return recuperacionService.correoConfigurado(); }

    public SolicitudRecuperacion solicitarCodigoRecuperacion(String usuarioOCorreo) {
        return recuperacionService.solicitar(usuarioOCorreo);
    }

    public void restablecerPassword(String usuarioOCorreo, String codigo, String nuevaPassword) {
        recuperacionService.restablecer(usuarioOCorreo, codigo, nuevaPassword);
    }

    public boolean guardar(Usuario usuario, String password) { return service.guardar(usuario, password); }
    public boolean registrar(Usuario usuario, String password) { return service.registrar(usuario, password); }
    public boolean actualizar(Usuario usuario, String password) { return service.actualizar(usuario, password); }
    public boolean cambiarEstado(int id, EstadoRegistro nuevoEstado) { return service.cambiarEstado(id, nuevoEstado); }
    public List<Usuario> listar() { return service.listar(); }
    public Optional<Usuario> buscar(int id) { return service.buscar(id); }
    public Optional<Usuario> buscarPorUsuario(String usuario) { return service.buscarPorUsuario(usuario); }
    public List<Rol> listarRoles() { return service.listarRoles(); }
}
