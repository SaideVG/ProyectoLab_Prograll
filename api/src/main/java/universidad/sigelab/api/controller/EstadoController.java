package universidad.sigelab.api.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * GET /api/estado: prueba de conexión que usa la app en la pantalla del
 * servidor. No pide token. Responde también si la base de datos contesta.
 */
@RestController
public class EstadoController {

    private final JdbcTemplate jdbc;

    public EstadoController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/api/estado")
    public Map<String, Object> estado() {
        boolean baseConectada;
        try {
            jdbc.queryForObject("SELECT 1", Integer.class);
            baseConectada = true;
        } catch (Exception e) {
            baseConectada = false;
        }
        return Map.of("servicio", "SIGELAB API", "baseDeDatos", baseConectada ? "conectada" : "sin conexión");
    }
}
