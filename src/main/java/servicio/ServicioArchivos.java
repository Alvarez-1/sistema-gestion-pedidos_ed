package servicio;

import modelo.Producto;
import modelo.Repartidor;
import estructura.ListaSimple;
import estructura.ColaPrioridad;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class ServicioArchivos {

    public ListaSimple<Producto> importarProductosCSV(String rutaArchivo) {
        ListaSimple<Producto> lista = new ListaSimple<>();
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            System.err.println("El archivo " + rutaArchivo + " no existe.");
            return lista;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean esPrimeraLinea = true;
            while ((linea = br.readLine()) != null) {
                if (esPrimeraLinea) {
                    esPrimeraLinea = false;
                    continue; // Saltar encabezado
                }
                if (linea.trim().isEmpty()) {
                    continue;
                }
                String[] tokens = linea.split(",");
                if (tokens.length >= 4) {
                    try {
                        int codigo = Integer.parseInt(tokens[0].trim());
                        String nombre = tokens[1].trim();
                        double precio = Double.parseDouble(tokens[2].trim());
                        int cantidad = Integer.parseInt(tokens[3].trim());
                        
                        Producto p = new Producto(codigo, nombre, precio, cantidad);
                        lista.insertarUltimo(p);
                    } catch (NumberFormatException e) {
                        System.err.println("Error al parsear línea de productos: " + linea + " -> " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error de lectura en productos: " + e.getMessage());
        }
        return lista;
    }

    public ColaPrioridad<Repartidor> importarRepartidoresCSV(String rutaArchivo) {
        ColaPrioridad<Repartidor> cola = new ColaPrioridad<>();
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            System.err.println("El archivo " + rutaArchivo + " no existe.");
            return cola;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean esPrimeraLinea = true;
            while ((linea = br.readLine()) != null) {
                if (esPrimeraLinea) {
                    esPrimeraLinea = false;
                    continue; // Saltar encabezado
                }
                if (linea.trim().isEmpty()) {
                    continue;
                }
                String[] tokens = linea.split(",");
                if (tokens.length >= 7) {
                    try {
                        long id = Long.parseLong(tokens[0].trim());
                        String nombre = tokens[1].trim();
                        long telefono = Long.parseLong(tokens[2].trim());
                        String zona = tokens[3].trim();
                        double calificacion = Double.parseDouble(tokens[4].trim());
                        double saldo = Double.parseDouble(tokens[5].trim());
                        boolean disponible = Boolean.parseBoolean(tokens[6].trim());
                        
                        Repartidor r = new Repartidor(calificacion, saldo, id, nombre, telefono, zona);
                        r.setDisponibilidad(disponible);
                        cola.encolar(r, disponible ? 1 : 0);
                    } catch (NumberFormatException e) {
                        System.err.println("Error al parsear línea de repartidores: " + linea + " -> " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error de lectura en repartidores: " + e.getMessage());
        }
        return cola;
    }

    public void exportarEstadisticasCSV(String rutaArchivo, ServicioAsignacion servicio) {
        File archivo = new File(rutaArchivo);
        // Valida que el directorio exista
        File parent = archivo.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            bw.write("Metrica,Valor");
            bw.newLine();
            
            if (servicio != null) {
                bw.write("Pedidos Entregados," + (servicio.getHistorialEntregas() != null ? servicio.getHistorialEntregas().getTamanio() : 0));
                bw.newLine();
                bw.write("Pedidos Cancelados," + servicio.getPedidosCancelados());
                bw.newLine();
                bw.write("Ganancias Admin," + servicio.getSaldo());
                bw.newLine();
                
                Repartidor mejorR = servicio.mejorRepartidor();
                bw.write("Repartidor Estrella," + (mejorR != null ? mejorR.getNombre() : "Ninguno"));
                bw.newLine();
                
                modelo.Cliente mejorC = servicio.mejorCliente();
                bw.write("Cliente Estrella," + (mejorC != null ? mejorC.getNombre() : "Ninguno"));
                bw.newLine();
                
                bw.write("Zona Mas Activa," + servicio.zonaMasPedidos());
                bw.newLine();
            } else {
                bw.write("Error,Servicio no inicializado");
                bw.newLine();
            }
            System.out.println("Estadísticas exportadas correctamente a: " + rutaArchivo);
        } catch (IOException e) {
            System.err.println("Error al exportar estadísticas: " + e.getMessage());
        }
    }
}
