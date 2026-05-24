package demo;

import configuracion.ConexionBD;

public class PruebaConexionBD {

    public static void main(String[] args) {
        System.out.println("--- INICIANDO PRUEBA DE CONEXIÓN A POSTGRESQL ---");
        
        ConexionBD conexionBD = new ConexionBD();
        boolean exito = conexionBD.probarConexion();
        
        if (exito) {
            System.out.println("Resultado: PRUEBA SUPERADA");
        } else {
            System.err.println("Resultado: PRUEBA FALLIDA");
            System.out.println("\nNota: Asegúrate de que el archivo 'config.properties' exista en la raíz del proyecto y tenga las credenciales correctas.");
        }
    }
}
