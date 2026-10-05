package universidad.sigelab.controller;

import universidad.sigelab.enums.EstadoRegistro;
import universidad.sigelab.model.Rol;
import universidad.sigelab.model.Usuario;
import universidad.sigelab.service.UsuarioService;
import universidad.sigelab.util.Sesion;

import java.util.List;
import java.util.Optional;

public class UsuarioController {
    private final UsuarioService service;
    public UsuarioController() { this(new UsuarioService()); }
    public UsuarioController(UsuarioService service) { this.service = service; }

    public Usuario iniciarSesion(String usuario, String password) {
        Usuario autenticado = service.autenticar(usuario, password);
        Sesion.iniciar(autenticado);
        return autenticado;
    }

    public void cerrarSesion() { Sesion.cerrar(); }

    public boolean guardar(Usuario usuario, String password) { return service.guardar(usuario, password); }
    public boolean actualizar(Usuario usuario, String password) { return service.actualizar(usuario, password); }
    public boolean cambiarEstado(int id, EstadoRegistro nuevoEstado) { return service.cambiarEstado(id, nuevoEstado); }
    public List<Usuario> listar() { return service.listar(); }
    public Optional<Usuario> buscar(int id) { return service.buscar(id); }
    public Optional<Usuario> buscarPorUsuario(String usuario) { return service.buscarPorUsuario(usuario); }
    public List<Rol> listarRoles() { return service.listarRoles(); }
}
