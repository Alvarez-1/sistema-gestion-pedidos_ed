package demo;

import estructura.ColaPrioridad;
import estructura.ListaSimple;
import java.util.HashMap;
import modelo.Comercio;
import modelo.Repartidor;
import servicio.ServicioDatosBD;

public class PruebaCargaEstructurasBD {

    public static void main(String[] args) {
        ServicioDatosBD servicio = new ServicioDatosBD();

        System.out.println("Iniciando carga de estructuras desde PostgreSQL...");

        ListaSimple<Comercio> comercios = servicio.cargarComercios();
        ColaPrioridad<Repartidor> repartidores = servicio.cargarRepartidores();
        HashMap<Long, Repartidor> mapaRepartidores = servicio.cargarMapaRepartidores();

        System.out.println("\n--- LISTA DE COMERCIOS Y PRODUCTOS ---");
        if (comercios != null && !comercios.esVacia()) {
            System.out.println(comercios.mostrar());
        } else {
            System.out.println("No se encontraron comercios.");
        }

        System.out.println("\n--- COLA DE PRIORIDAD DE REPARTIDORES ---");
        if (repartidores != null && !repartidores.esVacia()) {
            System.out.println(repartidores.mostrarCola());
        } else {
            System.out.println("No se encontraron repartidores.");
        }

        System.out.println("\n--- MAPA DE REPARTIDORES (por ID) ---");
        if (mapaRepartidores != null && !mapaRepartidores.isEmpty()) {
            mapaRepartidores.forEach((id, r) -> System.out.println("ID: " + id + " -> " + r.getNombre()));
        } else {
            System.out.println("No se encontraron repartidores en el mapa.");
        }

        System.out.println("\n--- RESUMEN ---");
        servicio.mostrarResumenCarga(comercios, repartidores);
        System.out.println("Prueba de carga finalizada.");
    }
}
