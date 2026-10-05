package universidad.sigelab.service;

import universidad.sigelab.enums.AccionAuditoria;
import universidad.sigelab.enums.Modulo;
import universidad.sigelab.model.Auditoria;
import universidad.sigelab.model.Usuario;
import universidad.sigelab.repository.AuditoriaRepository;
import universidad.sigelab.util.Sesion;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AuditoriaService {
    private static final int LARGO_DESCRIPCION = 500;

    private final AuditoriaRepository repository;
    public AuditoriaService() { this(new AuditoriaRepository()); }
    public AuditoriaService(AuditoriaRepository repository) { this.repository = repository; }

    /** usuario e idRegistro pueden ser null cuando la acción no tiene a quién o a qué asociarse. */
    public void registrar(Usuario usuario, AccionAuditoria accion, String entidad, Integer idRegistro, String descripcion) {
        repository.registrar(new Auditoria(usuario, accion, entidad, idRegistro, recortar(descripcion)));
    }

    /**
     * RN-18 dentro de una transacción: si la operación principal se deshace,
     * su registro de auditoría también, y viceversa.
     */
    public void registrar(Connection connection, Usuario usuario, AccionAuditoria accion, String entidad,
                          Integer idRegistro, String descripcion) throws SQLException {
        repository.registrar(connection, new Auditoria(usuario, accion, entidad, idRegistro, recortar(descripcion)));
    }

    /** Con accion en null devuelve los eventos de todas las acciones. */
    public List<Auditoria> listar(AccionAuditoria accion) {
        Sesion.exigirAcceso(Modulo.AUDITORIA);
        return accion == null ? repository.listar() : repository.listarPorAccion(accion);
    }

    private String recortar(String descripcion) {
        if (descripcion == null || descripcion.length() <= LARGO_DESCRIPCION) return descripcion;
        return descripcion.substring(0, LARGO_DESCRIPCION);
    }
}
