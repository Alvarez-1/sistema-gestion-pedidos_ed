package servicio;

import configuracion.ConexionBD;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ServicioCargaInicialBD {

    private final ConexionBD conexionBD;

    public ServicioCargaInicialBD() {
        this.conexionBD = new ConexionBD();
    }

    public void cargarComerciosDesdeCSV(String rutaArchivo) {
        String sql = "INSERT INTO comercios (id_comercio, nombre, tipo_negocio, direccion, zona) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON CONFLICT (id_comercio) " +
                     "DO UPDATE SET " +
                     "    nombre = EXCLUDED.nombre, " +
                     "    tipo_negocio = EXCLUDED.tipo_negocio, " +
                     "    direccion = EXCLUDED.direccion, " +
                     "    zona = EXCLUDED.zona;";

        try (Connection conn = conexionBD.obtenerConexion();
             BufferedReader br = new BufferedReader(new FileReader(rutaArchivo));
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String linea;
            while ((linea = br.readLine()) != null) {
                String lineaTrim = linea.trim();
                if (lineaTrim.isEmpty() || lineaTrim.startsWith("id_comercio")) continue;

                String[] datos = lineaTrim.split(",");
                if (datos.length != 5) {
                    System.err.println("Error de formato (columnas insuficientes): " + lineaTrim);
                    continue;
                }

                try {
                    pstmt.setInt(1, Integer.parseInt(datos[0].trim()));
                    pstmt.setString(2, datos[1].trim());
                    pstmt.setString(3, datos[2].trim());
                    pstmt.setString(4, datos[3].trim());
                    pstmt.setString(5, datos[4].trim());
                    pstmt.executeUpdate();
                } catch (NumberFormatException | SQLException e) {
                    System.err.println("Error procesando datos en línea: " + lineaTrim + " - " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo " + rutaArchivo + ": " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error de base de datos: " + e.getMessage());
        }
    }

    public void cargarProductosDesdeCSV(String rutaArchivo) {
        String sql = "INSERT INTO productos (id_producto, nombre, precio, cantidad, id_comercio) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON CONFLICT (id_producto) " +
                     "DO UPDATE SET " +
                     "    nombre = EXCLUDED.nombre, " +
                     "    precio = EXCLUDED.precio, " +
                     "    cantidad = EXCLUDED.cantidad, " +
                     "    id_comercio = EXCLUDED.id_comercio;";

        try (Connection conn = conexionBD.obtenerConexion();
             BufferedReader br = new BufferedReader(new FileReader(rutaArchivo));
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String linea;
            while ((linea = br.readLine()) != null) {
                String lineaTrim = linea.trim();
                if (lineaTrim.isEmpty() || lineaTrim.startsWith("id_producto") || lineaTrim.startsWith("codigo")) continue;

                String[] datos = lineaTrim.split(",");
                if (datos.length != 5) {
                    continue; // Ignoramos líneas que no coinciden con el formato esperado
                }

                try {
                    pstmt.setInt(1, Integer.parseInt(datos[0].trim()));
                    pstmt.setString(2, datos[1].trim());
                    pstmt.setDouble(3, Double.parseDouble(datos[2].trim()));
                    pstmt.setInt(4, Integer.parseInt(datos[3].trim()));
                    pstmt.setInt(5, Integer.parseInt(datos[4].trim()));
                    pstmt.executeUpdate();
                } catch (NumberFormatException | SQLException e) {
                    System.err.println("Error procesando datos en línea: " + lineaTrim + " - " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo " + rutaArchivo + ": " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error de base de datos: " + e.getMessage());
        }
    }

    public void cargarRepartidoresDesdeCSV(String rutaArchivo) {
        String sql = "INSERT INTO repartidores (id_repartidor, nombre, telefono, zona, calificacion, saldo, disponible) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (id_repartidor) " +
                     "DO UPDATE SET " +
                     "    nombre = EXCLUDED.nombre, " +
                     "    telefono = EXCLUDED.telefono, " +
                     "    zona = EXCLUDED.zona, " +
                     "    calificacion = EXCLUDED.calificacion, " +
                     "    saldo = EXCLUDED.saldo, " +
                     "    disponible = EXCLUDED.disponible;";

        try (Connection conn = conexionBD.obtenerConexion();
             BufferedReader br = new BufferedReader(new FileReader(rutaArchivo));
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String linea;
            while ((linea = br.readLine()) != null) {
                String lineaTrim = linea.trim();
                if (lineaTrim.isEmpty() || lineaTrim.startsWith("id_repartidor") || lineaTrim.startsWith("calificacion")) continue;

                String[] datos = lineaTrim.split(",");
                if (datos.length != 7) {
                    continue;
                }

                try {
                    pstmt.setLong(1, Long.parseLong(datos[0].trim()));
                    pstmt.setString(2, datos[1].trim());
                    pstmt.setLong(3, Long.parseLong(datos[2].trim()));
                    pstmt.setString(4, datos[3].trim());
                    pstmt.setDouble(5, Double.parseDouble(datos[4].trim()));
                    pstmt.setDouble(6, Double.parseDouble(datos[5].trim()));
                    pstmt.setBoolean(7, Boolean.parseBoolean(datos[6].trim()));
                    pstmt.executeUpdate();
                } catch (NumberFormatException | SQLException e) {
                    System.err.println("Error procesando datos en línea: " + lineaTrim + " - " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo " + rutaArchivo + ": " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error de base de datos: " + e.getMessage());
        }
    }

    public void cargarTodo() {
        System.out.println("Cargando comercios...");
        cargarComerciosDesdeCSV("data/comercios.csv");
        System.out.println("Comercios cargados correctamente.");

        System.out.println("Cargando productos...");
        cargarProductosDesdeCSV("data/productos.csv");
        System.out.println("Productos cargados correctamente.");

        System.out.println("Cargando repartidores...");
        cargarRepartidoresDesdeCSV("data/repartidores.csv");
        System.out.println("Repartidores cargados correctamente.");

        System.out.println("Carga inicial finalizada.");
    }
}
