package universidad.sigelab.repository;

import universidad.sigelab.enums.CondicionDevolucion;
import universidad.sigelab.enums.EstadoPrestamo;
import universidad.sigelab.model.DetallePrestamo;
import universidad.sigelab.model.Devolucion;
import universidad.sigelab.model.HistorialEquipo;
import universidad.sigelab.model.Prestamo;
import universidad.sigelab.model.PrestamoResumen;
import universidad.sigelab.util.JdbcSupport;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PrestamoRepository {

    /*
     * Encabezado del préstamo + cuántos equipos tiene y cuántos faltan por devolver.
     * Un detalle sin fila en Devolucion (LEFT JOIN con dv NULL) es un equipo pendiente,
     * por eso pendientes = equipos del préstamo - devoluciones registradas.
     */
    private static final String RESUMEN = """
            SELECT p.id_prestamo, p.solicitante_nombre, p.solicitante_identificacion, p.fecha_prestamo,
                   p.fecha_esperada_devolucion, p.estado, p.observaciones,
                   u.id_usuario AS registra_id, u.usuario AS registra_usuario,
                   u.nombre AS registra_nombre, u.apellido AS registra_apellido,
                   COUNT(d.id_detalle_prestamo) AS total_equipos,
                   COUNT(d.id_detalle_prestamo) - COUNT(dv.id_devolucion) AS pendientes
            FROM Prestamo p
            INNER JOIN Usuario u ON u.id_usuario = p.id_usuario_registra
            INNER JOIN DetallePrestamo d ON d.id_prestamo = p.id_prestamo
            LEFT JOIN Devolucion dv ON dv.id_detalle_prestamo = d.id_detalle_prestamo
            """;

    private static final String AGRUPAR = """
            GROUP BY p.id_prestamo, p.solicitante_nombre, p.solicitante_identificacion, p.fecha_prestamo,
                     p.fecha_esperada_devolucion, p.estado, p.observaciones,
                     u.id_usuario, u.usuario, u.nombre, u.apellido
            """;

    private static final String DETALLES = "SELECT d.id_detalle_prestamo, d.id_prestamo, dv.id_devolucion, " +
            "dv.fecha_devolucion, dv.condicion, dv.observacion AS devolucion_observacion, " + EquipoRepository.COLUMNAS +
            "FROM DetallePrestamo d " +
            "INNER JOIN Equipo e ON e.id_equipo = d.id_equipo " + EquipoRepository.JOINS +
            "LEFT JOIN Devolucion dv ON dv.id_detalle_prestamo = d.id_detalle_prestamo ";

    /*
     * ========================================================
     * ESCRITURA (siempre dentro de una transacción)
     * ========================================================
     */

    public void guardar(Connection connection, Prestamo prestamo) throws SQLException {
        String sql = "INSERT INTO Prestamo (solicitante_nombre, solicitante_identificacion, fecha_prestamo, " +
                "fecha_esperada_devolucion, id_usuario_registra, estado, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?)";
        prestamo.setIdPrestamo(JdbcSupport.insertar(connection, sql,
                prestamo.getSolicitanteNombre(), prestamo.getSolicitanteIdentificacion(),
                prestamo.getFechaPrestamo(), prestamo.getFechaEsperadaDevolucion(),
                prestamo.getUsuarioRegistra().getIdUsuario(), prestamo.getEstado(), prestamo.getObservaciones()));
    }

    public void guardarDetalle(Connection connection, int idPrestamo, DetallePrestamo detalle) throws SQLException {
        detalle.setIdPrestamo(idPrestamo);
        detalle.setIdDetallePrestamo(JdbcSupport.insertar(connection,
                "INSERT INTO DetallePrestamo (id_prestamo, id_equipo) VALUES (?, ?)",
                idPrestamo, detalle.getEquipo().getIdEquipo()));
    }

    /** Equipos del préstamo que todavía no tienen devolución registrada. */
    public int contarPendientes(Connection connection, int idPrestamo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM DetallePrestamo d " +
                "LEFT JOIN Devolucion dv ON dv.id_detalle_prestamo = d.id_detalle_prestamo " +
                "WHERE d.id_prestamo = ? AND dv.id_devolucion IS NULL";
        return JdbcSupport.contar(connection, sql, idPrestamo);
    }

    public void cambiarEstado(Connection connection, int idPrestamo, EstadoPrestamo estado) throws SQLException {
        JdbcSupport.actualizar(connection, "UPDATE Prestamo SET estado = ? WHERE id_prestamo = ?", estado, idPrestamo);
    }

    /*
     * ========================================================
     * CONSULTAS
     * ========================================================
     */

    public Optional<PrestamoResumen> buscarResumen(int idPrestamo) {
        return JdbcSupport.buscar("buscar el préstamo",
                RESUMEN + "WHERE p.id_prestamo = ? " + AGRUPAR, PrestamoRepository::mapearResumen, idPrestamo);
    }

    public List<PrestamoResumen> listarPorEstado(EstadoPrestamo estado) {
        return JdbcSupport.consultar("consultar préstamos",
                RESUMEN + "WHERE p.estado = ? " + AGRUPAR + "ORDER BY p.fecha_prestamo DESC",
                PrestamoRepository::mapearResumen, estado);
    }

    /**
     * RN-17: vencido = la fecha esperada ya pasó (WHERE) y todavía tiene equipos pendientes (HAVING).
     * "ahora" llega desde Java para comparar con el reloj de la aplicación y no con el del servidor.
     */
    public List<PrestamoResumen> listarVencidos(LocalDateTime ahora) {
        return JdbcSupport.consultar("consultar préstamos vencidos",
                RESUMEN + "WHERE p.fecha_esperada_devolucion < ? " + AGRUPAR +
                        "HAVING COUNT(d.id_detalle_prestamo) > COUNT(dv.id_devolucion) " +
                        "ORDER BY p.fecha_esperada_devolucion",
                PrestamoRepository::mapearResumen, ahora);
    }

    public List<PrestamoResumen> listarEntreFechas(LocalDateTime desde, LocalDateTime hasta) {
        return JdbcSupport.consultar("consultar préstamos entre fechas",
                RESUMEN + "WHERE p.fecha_prestamo BETWEEN ? AND ? " + AGRUPAR + "ORDER BY p.fecha_prestamo",
                PrestamoRepository::mapearResumen, desde, hasta);
    }

    public int contarActivos() {
        return JdbcSupport.contar("contar préstamos activos", "SELECT COUNT(*) FROM Prestamo WHERE estado = 'ACTIVO'");
    }

    public int contarVencidos(LocalDateTime ahora) {
        return listarVencidos(ahora).size();
    }

    /** Equipos de un préstamo, cada uno con su devolución si ya fue devuelto. */
    public List<DetallePrestamo> listarDetalles(int idPrestamo) {
        return JdbcSupport.consultar("consultar los equipos del préstamo",
                DETALLES + "WHERE d.id_prestamo = ? ORDER BY d.id_detalle_prestamo",
                PrestamoRepository::mapearDetalle, idPrestamo);
    }

    public Optional<DetallePrestamo> buscarDetalle(int idDetallePrestamo) {
        return JdbcSupport.buscar("buscar el equipo del préstamo",
                DETALLES + "WHERE d.id_detalle_prestamo = ?", PrestamoRepository::mapearDetalle, idDetallePrestamo);
    }

    /** Todos los préstamos en los que participó un equipo, del más reciente al más antiguo. */
    public List<HistorialEquipo> historialDeEquipo(int idEquipo) {
        String sql = """
                SELECT p.id_prestamo, p.solicitante_nombre, p.solicitante_identificacion, p.fecha_prestamo,
                       p.fecha_esperada_devolucion, dv.fecha_devolucion, dv.condicion
                FROM DetallePrestamo d
                INNER JOIN Prestamo p ON p.id_prestamo = d.id_prestamo
                LEFT JOIN Devolucion dv ON dv.id_detalle_prestamo = d.id_detalle_prestamo
                WHERE d.id_equipo = ?
                ORDER BY p.fecha_prestamo DESC
                """;
        return JdbcSupport.consultar("consultar el historial del equipo", sql, resultSet ->
                new HistorialEquipo(resultSet.getInt("id_prestamo"), resultSet.getString("solicitante_nombre"),
                        resultSet.getString("solicitante_identificacion"),
                        JdbcSupport.localDateTime(resultSet, "fecha_prestamo"),
                        JdbcSupport.localDateTime(resultSet, "fecha_esperada_devolucion"),
                        JdbcSupport.localDateTime(resultSet, "fecha_devolucion"),
                        condicion(resultSet.getString("condicion"))), idEquipo);
    }

    private static PrestamoResumen mapearResumen(ResultSet resultSet) throws SQLException {
        Prestamo prestamo = new Prestamo(resultSet.getInt("id_prestamo"), resultSet.getString("solicitante_nombre"),
                resultSet.getString("solicitante_identificacion"),
                JdbcSupport.localDateTime(resultSet, "fecha_prestamo"),
                JdbcSupport.localDateTime(resultSet, "fecha_esperada_devolucion"),
                UsuarioRepository.mapearBasico(resultSet, "registra"),
                EstadoPrestamo.valueOf(resultSet.getString("estado")), resultSet.getString("observaciones"));
        return new PrestamoResumen(prestamo, resultSet.getInt("total_equipos"), resultSet.getInt("pendientes"));
    }

    private static DetallePrestamo mapearDetalle(ResultSet resultSet) throws SQLException {
        int idDetalle = resultSet.getInt("id_detalle_prestamo");
        Devolucion devolucion = null;
        if (resultSet.getObject("id_devolucion") != null) {
            devolucion = new Devolucion(resultSet.getInt("id_devolucion"), idDetalle,
                    JdbcSupport.localDateTime(resultSet, "fecha_devolucion"),
                    condicion(resultSet.getString("condicion")),
                    resultSet.getString("devolucion_observacion"), null);
        }
        return new DetallePrestamo(idDetalle, resultSet.getInt("id_prestamo"),
                EquipoRepository.mapear(resultSet), devolucion);
    }

    private static CondicionDevolucion condicion(String valor) {
        return valor == null ? null : CondicionDevolucion.valueOf(valor);
    }
}
