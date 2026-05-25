package servicio;

import configuracion.ConexionBD;
import modelo.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ServicioClienteBD {

    private final ConexionBD conexionBD;

    public ServicioClienteBD() {
        this.conexionBD = new ConexionBD();
    }

    public Cliente buscarClientePorChatId(long chatId) {
        String sql = "SELECT id_cliente, nombre, telefono, direccion, zona, vip, penalizacion FROM clientes WHERE chat_id_telegram = ?";
        
        try (Connection conn = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, chatId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id_cliente");
                    String nombre = rs.getString("nombre");
                    long telefono = rs.getLong("telefono");
                    String direccion = rs.getString("direccion");
                    String zona = rs.getString("zona");
                    boolean vip = rs.getBoolean("vip");
                    double penalizacion = rs.getDouble("penalizacion");
                    
                    Cliente cliente = new Cliente(direccion, vip, id, nombre, telefono, zona);
                    cliente.setPenalizacion(penalizacion);
                    return cliente;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar cliente por ChatID: " + e.getMessage());
        }
        return null;
    }

    public void guardarOActualizarCliente(Cliente cliente, long chatId) {
        String sql = "INSERT INTO clientes (id_cliente, nombre, telefono, direccion, zona, vip, chat_id_telegram) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (chat_id_telegram) " +
                     "DO UPDATE SET " +
                     "    nombre = EXCLUDED.nombre, " +
                     "    telefono = EXCLUDED.telefono, " +
                     "    direccion = EXCLUDED.direccion, " +
                     "    zona = EXCLUDED.zona, " +
                     "    vip = EXCLUDED.vip;";
        
        try (Connection conn = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, cliente.getId());
            pstmt.setString(2, cliente.getNombre());
            pstmt.setLong(3, cliente.getTelefono());
            pstmt.setString(4, cliente.getDireccion());
            pstmt.setString(5, cliente.getZona());
            pstmt.setBoolean(6, cliente.isVip());
            pstmt.setLong(7, chatId);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al guardar/actualizar cliente: " + e.getMessage());
        }
    }
}
