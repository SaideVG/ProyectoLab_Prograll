package universidad.sigelab.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la API REST de SIGELAB.
 *
 * Flujo de una petición:
 *   App móvil -> Controller -> Service -> Repository -> SQL Server (SigelabDB)
 *
 * Es la misma organización en capas del proyecto de escritorio; la diferencia
 * es que aquí la "vista" es la app móvil, que recibe las respuestas en JSON.
 */
@SpringBootApplication
public class SigelabApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SigelabApiApplication.class, args);
    }
}
