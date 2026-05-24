package demo;

import servicio.ServicioCargaInicialBD;

public class PruebaCargaInicialBD {

    public static void main(String[] args) {
        ServicioCargaInicialBD servicio = new ServicioCargaInicialBD();
        servicio.cargarTodo();
    }
}
