package universidad.sigelab.api.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Escribe en la misma tabla Auditoria que el escritorio (RN-18), así los accesos
 * desde la app también aparecen en el módulo de Auditoría.
 */
@Repository
public class AuditoriaRepository {

    private static final int LARGO_DESCRIPCION = 500;

    private final JdbcTemplate jdbc;

    public AuditoriaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** idUsuario e idRegistro pueden ser null, igual que en el escritorio. */
    public void registrar(Integer idUsuario, String accion, String entidad, Integer idRegistro, String descripcion) {
        jdbc.update("INSERT INTO Auditoria (id_usuario, fecha_hora, accion, entidad, id_registro, descripcion) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                idUsuario, LocalDateTime.now(), accion, entidad, idRegistro, recortar(descripcion));
    }

    private String recortar(String descripcion) {
        if (descripcion == null || descripcion.length() <= LARGO_DESCRIPCION) return descripcion;
        return descripcion.substring(0, LARGO_DESCRIPCION);
    }
}
