package universidad.sigelab.repository;

import universidad.sigelab.model.Devolucion;
import universidad.sigelab.util.JdbcSupport;

import java.sql.Connection;
import java.sql.SQLException;

public class DevolucionRepository {

    /**
     * Registra la devolución de un equipo. Siempre se usa dentro de una transacción.
     * La restricción UQ_Devolucion_Detalle impide una segunda fila para el mismo equipo del préstamo.
     */
    public void guardar(Connection connection, Devolucion devolucion) throws SQLException {
        String sql = "INSERT INTO Devolucion (id_detalle_prestamo, fecha_devolucion, condicion, observacion, " +
                "id_usuario_recibe) VALUES (?, ?, ?, ?, ?)";
        devolucion.setIdDevolucion(JdbcSupport.insertar(connection, sql,
                devolucion.getIdDetallePrestamo(), devolucion.getFechaDevolucion(), devolucion.getCondicion(),
                devolucion.getObservacion(), devolucion.getUsuarioRecibe().getIdUsuario()));
    }
}
