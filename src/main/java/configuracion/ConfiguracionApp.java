package configuracion;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Properties;

public class ConfiguracionApp {

    private Properties properties;
    private static final String ARCHIVO_CONFIGURACION = "config.properties";

    public ConfiguracionApp() {
        properties = new Properties();
        cargarPropiedades();
    }

    private void cargarPropiedades() {
        try (FileInputStream fis = new FileInputStream(ARCHIVO_CONFIGURACION)) {
            properties.load(fis);
        } catch (FileNotFoundException e) {
            System.err.println("ERROR CRÍTICO: No se encontró el archivo '" + ARCHIVO_CONFIGURACION + "'.");
            System.err.println("Por favor, crea el archivo '" + ARCHIVO_CONFIGURACION + "' en la raíz del proyecto basándote en 'config-example.properties'.");
        } catch (IOException e) {
            System.err.println("ERROR al leer el archivo de configuración '" + ARCHIVO_CONFIGURACION + "': " + e.getMessage());
        }
    }

    public String getTelegramBotUsername() {
        return properties.getProperty("telegram.bot.username", "");
    }

    public String getTelegramBotToken() {
        return properties.getProperty("telegram.bot.token", "");
    }

    public String getDbUrl() {
        return properties.getProperty("db.url", "");
    }

    public String getDbUser() {
        return properties.getProperty("db.user", "");
    }

    public String getDbPassword() {
        return properties.getProperty("db.password", "");
    }
}
