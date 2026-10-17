package universidad.sigelab.service;

import universidad.sigelab.enums.AccionAuditoria;
import universidad.sigelab.model.Usuario;
import universidad.sigelab.repository.UsuarioRostroRepository;
import universidad.sigelab.util.BusinessException;

import java.util.Optional;

/** Reglas de negocio del rostro del usuario (RN-02). */
public class UsuarioRostroService {

    private static final String ENTIDAD = "UsuarioRostro";

    private final UsuarioRostroRepository repository;
    private final AuditoriaService auditoriaService;

    public UsuarioRostroService() { this(new UsuarioRostroRepository(), new AuditoriaService()); }

    public UsuarioRostroService(UsuarioRostroRepository repository, AuditoriaService auditoriaService) {
        this.repository = repository;
        this.auditoriaService = auditoriaService;
    }

    public boolean tieneRostro(int idUsuario) {
        return repository.existe(idUsuario);
    }

    public Optional<float[]> buscar(int idUsuario) {
        return repository.buscar(idUsuario);
    }

    public void registrar(Usuario usuario, float[] vector) {
        if (usuario == null || vector == null || vector.length != ReconocimientoFacial.LARGO_VECTOR) {
            throw new BusinessException("No se pudo obtener un rostro válido. Intente de nuevo.");
        }
        repository.guardar(usuario.getIdUsuario(), vector);
        auditoriaService.registrar(usuario, AccionAuditoria.ROSTRO_REGISTRADO, ENTIDAD,
                usuario.getIdUsuario(), "Rostro registrado para " + usuario.getUsuario() + ".");
    }

    public void eliminar(Usuario usuario) {
        repository.eliminar(usuario.getIdUsuario());
        auditoriaService.registrar(usuario, AccionAuditoria.ROSTRO_ELIMINADO, ENTIDAD,
                usuario.getIdUsuario(), "Rostro eliminado de " + usuario.getUsuario() + ".");
    }
}
