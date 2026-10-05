package universidad.sigelab.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = configuracion(
            "sigelab.db.url", "DB_URL",
            "jdbc:sqlserver://localhost:1433;databaseName=SigelabDB;encrypt=true;trustServerCertificate=true;sendTimeAsDatetime=false;");
    private static final String USER = configuracion("sigelab.db.user", "DB_USER", "sa");
    private static final String PASSWORD = configuracion("sigelab.db.password", "DB_PASSWORD", "Dev2025!");

    private DatabaseConnection() {
    }

    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }

    private static String configuracion(String propiedad, String variable, String valorPorDefecto) {
        String configurado = System.getProperty(propiedad);
        if (configurado == null || configurado.isBlank()) configurado = System.getenv(variable);
        return configurado == null || configurado.isBlank() ? valorPorDefecto : configurado;
    }
}
