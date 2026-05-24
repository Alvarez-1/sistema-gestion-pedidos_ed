package configuracion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private final ConfiguracionApp configuracion;

    public ConexionBD() {
        this.configuracion = new ConfiguracionApp();
    }

    public Connection obtenerConexion() throws SQLException {
        String url = configuracion.getDbUrl();
        String user = configuracion.getDbUser();
        String password = configuracion.getDbPassword();

        if (url == null || url.isEmpty() || user == null || user.isEmpty()) {
            throw new SQLException("Configuración de base de datos incompleta en config.properties");
        }

        return DriverManager.getConnection(url, user, password);
    }

    public boolean probarConexion() {
        try (Connection conn = obtenerConexion()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Conexión exitosa a PostgreSQL.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("No se pudo conectar a PostgreSQL: " + e.getMessage());
        }
        return false;
    }
}
