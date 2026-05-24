package demo;

import modelo.*;
import estructura.*;
import servicio.*;

public class NewMain {

    static void guardarRepartidor(Repartidor r, ServicioAsignacion a) {
    a.getUsuariosRepartidores().put(r.getId(), r);
    a.getRepartidores().encolar(r, r.isDisponibilidad() ? 1 : 0);
    }
    
    static void guardarUsuario(Cliente c, ServicioAsignacion a) {
    a.getClientes().put(c.getId(), c);
    }
    
    static void guardarPedidoPendientes(Pedido p, ServicioAsignacion a) {
        if (p == null) {
            System.out.println("El pedido es nulo, no se puede guardar.");
            return;
        }
        if (!p.tieneProductos()) {
            System.out.println("El pedido " + p.getCodigo() + " no se puede registrar porque no tiene productos validos.");
            return;
        }
        a.getPedidosPendientes().encolar(p, p.getUsuario().isVip() ? 1 : 0);
        a.registrarPedidoEnMapa(p);
    }
    
    static void guardarProducto(Pedido p, int Codigo, int Cantidad) {
    p.agregarProducto(Codigo, Cantidad);
    }

    static void guardarProductoLista(ListaSimple<Producto> ls, Producto p) {
    ls.insertarUltimo(p);
    }
    
    public static void main(String[] args) {
        //usuarios
        Cliente usuario1 = new Cliente("carrera", false, 1, "Daniel", 11, "Sur");
        Cliente usuario2 = new Cliente("carrera", true, 2, "Steven", 21, "Norte");
        Cliente usuario3 = new Cliente("carrera", false, 3, "Sebastian", 31, "Este");
        Cliente usuario4 = new Cliente("carrera", true, 4, "Camilo", 41, "Oeste");
        Cliente usuario5 = new Cliente("carrera", false, 5, "Maria", 51, "Sur");
        Cliente usuario6 = new Cliente("carrera", true, 6, "Bella", 61, "Norte");
        Cliente usuario7 = new Cliente("carrera", false, 7, "Camila", 71, "Este");
        Cliente usuario8 = new Cliente("carrera", true, 8, "Fabian", 81, "Oeste");
        Cliente usuario9 = new Cliente("carrera", false, 9, "Fernando", 91, "Sur");
        //repartidores
        Repartidor usuario10 = new Repartidor(4.5, 0, 10, "Alejandro", 101, "Sur");
        Repartidor usuario11 = new Repartidor(3.5, 0, 11, "Camila", 111, "Norte");
        Repartidor usuario12 = new Repartidor(2.6, 0, 12, "Mariano", 121, "Este");
        Repartidor usuario13 = new Repartidor(5, 0, 13, "Carlos", 131, "Oeste");
        Repartidor usuario14 = new Repartidor(4, 0, 14, "Juan", 141, "Sur");
        //productos
        Producto Producto1 = new Producto(1, "peluche", 20000, 4);
        Producto Producto2 = new Producto(2, "Ladrillo", 2000, 1000);
        Producto Producto3 = new Producto(3, "laminas", 30000, 100);
        Producto Producto4 = new Producto(4, "cemento", 45000, 20);
        Producto Producto5 = new Producto(5, "varilla", 8500, 40);
        Producto Producto6 = new Producto(6, "desayuno", 20000, 10);
        Producto Producto7 = new Producto(7, "rosas", 40000, 5);
        Producto Producto8 = new Producto(8, "detalles pequeños", 25000, 7);
        Producto Producto9 = new Producto(9, "detalles grandes", 50000, 4);
        Producto Producto10 = new Producto(10, "colchones", 85000, 5);
        Producto Producto11 = new Producto(11, "cobijas", 37000, 4);
        Producto Producto12 = new Producto(12, "almohadas", 10000, 8);
        //lista de tiendas con sus productos
        ListaSimple<Producto> listaConstructora = new ListaSimple<>();
        ListaSimple<Producto> listaDetalles = new ListaSimple<>();
        ListaSimple<Producto> listaColchones = new ListaSimple<>();
        guardarProductoLista(listaConstructora, Producto2);
        guardarProductoLista(listaConstructora, Producto3);
        guardarProductoLista(listaConstructora, Producto4);
        guardarProductoLista(listaConstructora, Producto5);
        guardarProductoLista(listaDetalles, Producto1);
        guardarProductoLista(listaDetalles, Producto6);
        guardarProductoLista(listaDetalles, Producto7);
        guardarProductoLista(listaDetalles, Producto8);
        guardarProductoLista(listaDetalles, Producto9);
        guardarProductoLista(listaColchones, Producto10);
        guardarProductoLista(listaColchones, Producto11);
        guardarProductoLista(listaColchones, Producto12);
        
        //tiendas como tal
        Comercio Constructora = new Comercio(1, "Constructora", "Venta de materiales para la construcción", "carrera", "Norte", listaConstructora);
        Comercio Decoracion = new Comercio(2, "Decoraciones", "Venta de decoraciones para dias especiales", "carrera", "Sur", listaDetalles);
        Comercio Colchon = new Comercio(3, "Colchoneria", "Venta de articulos de la comodidad de la habitacion", "carrera", "Este", listaColchones);


        //pedidos y agregacion de productos elegidos
        Pedido pedido1 = new Pedido(1, usuario1, Constructora);
        Pedido pedido2 = new Pedido(2, usuario4, Colchon);
        Pedido pedido3 = new Pedido(3, usuario6, Decoracion);
        Pedido pedido4 = new Pedido(4, usuario9, Constructora);

        guardarProducto(pedido1, 2, 5);
        guardarProducto(pedido1, 2, 5);
        guardarProducto(pedido1, 67, 5);
        guardarProducto(pedido1, 3, 1);
        guardarProducto(pedido1, 3, 1);
        pedido1.eliminacionProducto(3, 1);
        pedido1.eliminacionProducto(2, 3);
        guardarProducto(pedido2, 10, 10);
        guardarProducto(pedido2, 11, 2);
        guardarProducto(pedido2, 12, 4);
        guardarProducto(pedido3, 6, 15);
        guardarProducto(pedido3, 7, 15);
        guardarProducto(pedido3, 8, 15);
        guardarProducto(pedido3, 9, 15);
        guardarProducto(pedido4, 3, 500);
        guardarProducto(pedido4, 2, 500);
        guardarProducto(pedido4, 4, 500);
        
        //administracion y asignacion de repartidores
        usuario14.setDisponibilidad(false);

        ServicioAsignacion admi = new ServicioAsignacion();
        
        guardarRepartidor(usuario10, admi);
        guardarRepartidor(usuario11, admi);
        guardarRepartidor(usuario12, admi);
        guardarRepartidor(usuario13, admi);
        guardarRepartidor(usuario14, admi);
        
        guardarUsuario(usuario1, admi);
        guardarUsuario(usuario2, admi);
        guardarUsuario(usuario3, admi);
        guardarUsuario(usuario4, admi);
        guardarUsuario(usuario5, admi);
        guardarUsuario(usuario6, admi);
        guardarUsuario(usuario7, admi);
        guardarUsuario(usuario8, admi);
        guardarUsuario(usuario9, admi);
       
        guardarPedidoPendientes(pedido1, admi);
        guardarPedidoPendientes(pedido2, admi);
        guardarPedidoPendientes(pedido3, admi);
        guardarPedidoPendientes(pedido4, admi);
        
        System.out.println(admi);
        Cliente c = pedido2.getUsuario();
//        admi.cancelarPedido(pedido2);
        admi.asignacion();
        admi.avanzarEstado(pedido4);
        System.out.println("----------------");
        System.out.println(admi);
        admi.asignacion();
        admi.avanzarEstado(pedido4);
        System.out.println("----------------");
        System.out.println(admi);
        admi.cancelarPedido(pedido1);
        System.out.println(c.getHistorialPedidos());
        admi.asignacion();
        admi.avanzarEstado(pedido4);
        System.out.println("----------------");
        System.out.println(admi);
        Repartidor r = pedido4.getRappi();
        admi.avanzarEstado(pedido4);
        admi.calificarPedido(r, 4);
        System.out.println("----------------");
        System.out.println(admi);
        Repartidor t = admi.getUsuariosRepartidores().get(usuario10.getId());
        System.out.println(t.getSaldo());
        System.out.println(admi.getSaldo());
        
        admi.mostrarEstadisticas();

        Pedido pedidoBuscado = admi.buscarPedido(3);
        if (pedidoBuscado != null) {
            System.out.println(pedidoBuscado);
        } else {
            System.out.println("Pedido no encontrado o no registrado por no tener productos validos.");
        }
        
        System.out.println("\n----- PRUEBAS ARCHIVOS Y PDF -----");
        ServicioComprobante servicioComprobante = new ServicioComprobante();
        servicioComprobante.generarComprobantePedidoPDF(pedido4);
        
        ServicioArchivos servicioArchivos = new ServicioArchivos();
        servicioArchivos.exportarEstadisticasCSV("exportaciones/reporte_estadisticas.csv", admi);
        
        System.out.println("Importando CSV de prueba para verificar funcionamiento...");
        servicioArchivos.importarProductosCSV("data/productos.csv");
        servicioArchivos.importarRepartidoresCSV("data/repartidores.csv");
        System.out.println("¡Pruebas de archivos completadas con éxito!");
    }

}
