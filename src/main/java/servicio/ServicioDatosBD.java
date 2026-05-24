package servicio;

import configuracion.ConexionBD;
import estructura.ColaPrioridad;
import estructura.ListaSimple;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import modelo.Comercio;
import modelo.Producto;
import modelo.Repartidor;

public class ServicioDatosBD {

    private final ConexionBD conexionBD;

    public ServicioDatosBD() {
        this.conexionBD = new ConexionBD();
    }

    public ListaSimple<Comercio> cargarComercios() {
        ListaSimple<Comercio> listaComercios = new ListaSimple<>();
        String sql = "SELECT id_comercio, nombre, tipo_negocio, direccion, zona FROM comercios ORDER BY id_comercio;";

        try (Connection conn = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int idComercio = rs.getInt("id_comercio");
                String nombre = rs.getString("nombre");
                String tipoNegocio = rs.getString("tipo_negocio");
                String direccion = rs.getString("direccion");
                String zona = rs.getString("zona");

                ListaSimple<Producto> productos = cargarProductosPorComercio(idComercio);
                Comercio comercio = new Comercio(idComercio, nombre, tipoNegocio, direccion, zona, productos);
                listaComercios.insertarUltimo(comercio);
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar comercios desde la base de datos: " + e.getMessage());
        }
        return listaComercios;
    }

    public ListaSimple<Producto> cargarProductosPorComercio(int idComercio) {
        ListaSimple<Producto> listaProductos = new ListaSimple<>();
        String sql = "SELECT id_producto, nombre, precio, cantidad FROM productos WHERE id_comercio = ? ORDER BY id_producto;";

        try (Connection conn = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idComercio);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int idProducto = rs.getInt("id_producto");
                    String nombre = rs.getString("nombre");
                    double precio = rs.getDouble("precio");
                    int cantidad = rs.getInt("cantidad");

                    Producto producto = new Producto(idProducto, nombre, precio, cantidad);
                    listaProductos.insertarUltimo(producto);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar productos para el comercio " + idComercio + ": " + e.getMessage());
        }
        return listaProductos;
    }

    public ColaPrioridad<Repartidor> cargarRepartidores() {
        ColaPrioridad<Repartidor> colaRepartidores = new ColaPrioridad<>();
        String sql = "SELECT id_repartidor, nombre, telefono, zona, calificacion, saldo, disponible FROM repartidores ORDER BY id_repartidor;";

        try (Connection conn = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("id_repartidor");
                String nombre = rs.getString("nombre");
                long telefono = rs.getLong("telefono");
                String zona = rs.getString("zona");
                double calificacion = rs.getDouble("calificacion");
                double saldo = rs.getDouble("saldo");
                boolean disponible = rs.getBoolean("disponible");

                Repartidor repartidor = new Repartidor(calificacion, saldo, id, nombre, telefono, zona);
                repartidor.setDisponibilidad(disponible);
                
                // Prioridad: 1 si está disponible, 0 si no
                colaRepartidores.encolar(repartidor, disponible ? 1 : 0);
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar repartidores desde la base de datos: " + e.getMessage());
        }
        return colaRepartidores;
    }

    public HashMap<Long, Repartidor> cargarMapaRepartidores() {
        HashMap<Long, Repartidor> mapaRepartidores = new HashMap<>();
        String sql = "SELECT id_repartidor, nombre, telefono, zona, calificacion, saldo, disponible FROM repartidores ORDER BY id_repartidor;";

        try (Connection conn = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("id_repartidor");
                String nombre = rs.getString("nombre");
                long telefono = rs.getLong("telefono");
                String zona = rs.getString("zona");
                double calificacion = rs.getDouble("calificacion");
                double saldo = rs.getDouble("saldo");
                boolean disponible = rs.getBoolean("disponible");

                Repartidor repartidor = new Repartidor(calificacion, saldo, id, nombre, telefono, zona);
                repartidor.setDisponibilidad(disponible);
                
                mapaRepartidores.put(id, repartidor);
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar el mapa de repartidores: " + e.getMessage());
        }
        return mapaRepartidores;
    }

    public void mostrarResumenCarga(ListaSimple<Comercio> comercios, ColaPrioridad<Repartidor> repartidores) {
        System.out.println("Resumen de carga desde PostgreSQL:");
        System.out.println("Comercios cargados: " + (comercios != null ? comercios.getTamanio() : 0));
        System.out.println("Repartidores cargados: " + (repartidores != null ? repartidores.getTamanio() : 0));
    }
}
