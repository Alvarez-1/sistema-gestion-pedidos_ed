import servicio.ServicioAsignacion;
import servicio.ServicioDatosBD;

import javax.swing.SwingUtilities;


public class Main {

    public static void main(String[] args) {
        // Crear servicio
        ServicioAsignacion servicioAsignacion = new ServicioAsignacion();

        // Cargar datos
        ServicioDatosBD servicioDatosBD = new ServicioDatosBD();
        servicioAsignacion.setRepartidores(servicioDatosBD.cargarRepartidores());
        servicioAsignacion.setUsuariosRepartidores(servicioDatosBD.cargarMapaRepartidores());

       // Lanzar interfaz
        SwingUtilities.invokeLater(() -> {
            new Interfaz(servicioAsignacion);
        });
    }
}
