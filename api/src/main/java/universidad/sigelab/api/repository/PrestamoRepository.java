package universidad.sigelab.api.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import universidad.sigelab.api.dto.DetallePrestamoDto;
import universidad.sigelab.api.dto.HistorialPrestamoDto;
import universidad.sigelab.api.dto.PrestamoDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PrestamoRepository {

    /*
     * Misma consulta que PrestamoRepository del escritorio: encabezado del préstamo
     * + cuántos equipos tiene y cuántos faltan por devolver. Un detalle sin fila en
     * Devolucion (LEFT JOIN con dv NULL) es un equipo pendiente.
     */
    private static final String RESUMEN = """
            SELECT p.id_prestamo, p.solicitante_nombre, p.solicitante_identificacion, p.fecha_prestamo,
                   p.fecha_esperada_devolucion, p.estado, p.observaciones,
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
                     p.fecha_esperada_devolucion, p.estado, p.observaciones, u.nombre, u.apellido
            """;

    private static final String DETALLES = "SELECT d.id_detalle_prestamo, dv.id_devolucion, dv.fecha_devolucion, " +
            "dv.condicion, dv.observacion AS devolucion_observacion, " + EquipoRepository.COLUMNAS +
            "FROM DetallePrestamo d " +
            "INNER JOIN Equipo e ON e.id_equipo = d.id_equipo " + EquipoRepository.JOINS +
            "LEFT JOIN Devolucion dv ON dv.id_detalle_prestamo = d.id_detalle_prestamo ";

    private final JdbcTemplate jdbc;

    public PrestamoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** "ahora" llega desde el servicio para calcular si cada préstamo está vencido. */
    public List<PrestamoDto> listarPorEstado(String estado, LocalDateTime ahora) {
        return jdbc.query(RESUMEN + "WHERE p.estado = ? " + AGRUPAR + "ORDER BY p.fecha_prestamo DESC",
                (resultSet, fila) -> mapearResumen(resultSet, ahora), estado);
    }

    /** RN-17: vencido = la fecha esperada ya pasó (WHERE) y todavía tiene equipos pendientes (HAVING). */
    public List<PrestamoDto> listarVencidos(LocalDateTime ahora) {
        return jdbc.query(RESUMEN + "WHERE p.fecha_esperada_devolucion < ? " + AGRUPAR +
                        "HAVING COUNT(d.id_detalle_prestamo) > COUNT(dv.id_devolucion) " +
                        "ORDER BY p.fecha_esperada_devolucion",
                (resultSet, fila) -> mapearResumen(resultSet, ahora), ahora);
    }

    public Optional<PrestamoDto> buscar(int idPrestamo, LocalDateTime ahora) {
        return jdbc.query(RESUMEN + "WHERE p.id_prestamo = ? " + AGRUPAR,
                (resultSet, fila) -> mapearResumen(resultSet, ahora), idPrestamo).stream().findFirst();
    }

    public int contarActivos() {
        Integer cantidad = jdbc.queryForObject("SELECT COUNT(*) FROM Prestamo WHERE estado = 'ACTIVO'", Integer.class);
        return cantidad == null ? 0 : cantidad;
    }

    public int contarVencidos(LocalDateTime ahora) {
        return listarVencidos(ahora).size();
    }

    /** Equipos de un préstamo, cada uno con su devolución si ya fue devuelto. */
    public List<DetallePrestamoDto> listarDetalles(int idPrestamo) {
        return jdbc.query(DETALLES + "WHERE d.id_prestamo = ? ORDER BY d.id_detalle_prestamo",
                PrestamoRepository::mapearDetalle, idPrestamo);
    }

    /** Todos los préstamos en los que participó un equipo, del más reciente al más antiguo. */
    public List<HistorialPrestamoDto> historialDeEquipo(int idEquipo) {
        String sql = """
                SELECT p.id_prestamo, p.solicitante_nombre, p.solicitante_identificacion, p.fecha_prestamo,
                       p.fecha_esperada_devolucion, dv.fecha_devolucion, dv.condicion
                FROM DetallePrestamo d
                INNER JOIN Prestamo p ON p.id_prestamo = d.id_prestamo
                LEFT JOIN Devolucion dv ON dv.id_detalle_prestamo = d.id_detalle_prestamo
                WHERE d.id_equipo = ?
                ORDER BY p.fecha_prestamo DESC
                """;
        return jdbc.query(sql, (resultSet, fila) -> new HistorialPrestamoDto(resultSet.getInt("id_prestamo"),
                resultSet.getString("solicitante_nombre"), resultSet.getString("solicitante_identificacion"),
                fecha(resultSet, "fecha_prestamo"), fecha(resultSet, "fecha_esperada_devolucion"),
                fecha(resultSet, "fecha_devolucion"), resultSet.getString("condicion")), idEquipo);
    }

    private static PrestamoDto mapearResumen(ResultSet resultSet, LocalDateTime ahora) throws SQLException {
        LocalDateTime fechaEsperada = fecha(resultSet, "fecha_esperada_devolucion");
        int pendientes = resultSet.getInt("pendientes");
        boolean vencido = pendientes > 0 && fechaEsperada.isBefore(ahora);

        return new PrestamoDto(resultSet.getInt("id_prestamo"), resultSet.getString("solicitante_nombre"),
                resultSet.getString("solicitante_identificacion"), fecha(resultSet, "fecha_prestamo"),
                fechaEsperada, resultSet.getString("estado"), resultSet.getString("observaciones"),
                resultSet.getString("registra_nombre") + " " + resultSet.getString("registra_apellido"),
                resultSet.getInt("total_equipos"), pendientes, vencido);
    }

    private static DetallePrestamoDto mapearDetalle(ResultSet resultSet, int fila) throws SQLException {
        boolean devuelto = resultSet.getObject("id_devolucion") != null;
        return new DetallePrestamoDto(resultSet.getInt("id_detalle_prestamo"), EquipoRepository.mapear(resultSet, fila),
                devuelto, fecha(resultSet, "fecha_devolucion"), resultSet.getString("condicion"),
                resultSet.getString("devolucion_observacion"));
    }

    static LocalDateTime fecha(ResultSet resultSet, String columna) throws SQLException {
        return resultSet.getObject(columna, LocalDateTime.class);
    }
}
