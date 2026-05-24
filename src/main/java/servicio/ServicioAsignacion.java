package servicio;

import modelo.*;
import estructura.*;
import java.util.HashMap;

public class ServicioAsignacion {

    private ColaPrioridad<Pedido> pedidosPendientes;
    private Pila<Pedido> historialEntregas;
    private Pila<Pedido> historialCancelados;
    private ColaPrioridad<Repartidor> repartidores;
    private Cola<Pedido> pedidoParaEntregar;
    private HashMap<Long, Cliente> clientes;
    private HashMap<Long, Repartidor> usuariosRepartidores;
    private HashMap<Integer, Pedido> pedidosPorCodigo;
    private long saldo;
    private int pedidosCancelados;

    public ServicioAsignacion() {
        this.pedidosPendientes = new ColaPrioridad<>();
        this.repartidores = new ColaPrioridad<>();
        this.historialEntregas = new Pila<>();
        this.pedidoParaEntregar = new Cola<>();
        this.clientes = new HashMap<>();
        this.usuariosRepartidores = new HashMap<>();
        this.historialCancelados = new Pila<>();
        this.pedidosPorCodigo = new HashMap<>();
        this.saldo = 0;
        this.pedidosCancelados = 0;
    }

    public ColaPrioridad<Repartidor> getRepartidores() {
        return repartidores;
    }

    public void setRepartidores(ColaPrioridad<Repartidor> repartidores) {
        this.repartidores = repartidores;
    }

    public Pila<Pedido> getHistorialEntregas() {
        return historialEntregas;
    }

    public void setHistorialEntregas(Pila<Pedido> historialEntregas) {
        this.historialEntregas = historialEntregas;
    }

    public ColaPrioridad<Pedido> getPedidosPendientes() {
        return pedidosPendientes;
    }

    public void setPedidosPendientes(ColaPrioridad<Pedido> pedidosPendientes) {
        this.pedidosPendientes = pedidosPendientes;
    }

    public Cola<Pedido> getPedidoParaEntregar() {
        return pedidoParaEntregar;
    }

    public void setPedidoParaEntregar(Cola<Pedido> pedidoParaEntregar) {
        this.pedidoParaEntregar = pedidoParaEntregar;
    }

    public HashMap<Long, Cliente> getClientes() {
        return clientes;
    }

    public void setClientes(HashMap<Long, Cliente> clientes) {
        this.clientes = clientes;
    }

    public HashMap<Long, Repartidor> getUsuariosRepartidores() {
        return usuariosRepartidores;
    }

    public void setUsuariosRepartidores(HashMap<Long, Repartidor> usuariosRepartidores) {
        this.usuariosRepartidores = usuariosRepartidores;
    }

    public HashMap<Integer, Pedido> getPedidosPorCodigo() {
        return pedidosPorCodigo;
    }

    public void registrarPedidoEnMapa(Pedido pedido) {
        if (pedido != null) {
            pedidosPorCodigo.put(pedido.getCodigo(), pedido);
        }
    }

    @Override
    public String toString() {
        return "ADMINISTRACION DE PEDIDOS\n\n"
                + (pedidosPendientes.esVacia() ? "SIN PEDIDOS PENDIENTES POR REPARTIDOR...\n" : "PEDIDOS PENDIENTES POR REPARTIDOR\n\n"
                + pedidosPendientes)
                + "\n" + repartidores
                + "\n\n" + (pedidoParaEntregar.esVacia() ? "SIN PEDIDOS PARA ENTREGAR...\n" : "PEDIDOS PARA ENTREGAR\n\n" + pedidoParaEntregar)
                + "\n\n" + (historialEntregas.esVacia() ? "SIN HISTORIAL DE PEDIDOS ENTREGADOS...\n" : "HISTORIAL DE PEDIDOS ENTREGADOS\n\n" + historialEntregas)
                + "\n\n" + (historialCancelados.esVacia() ? "SIN HISTORIAL DE PEDIDOS CANCELADOS...\n" : "HISTORIAL DE PEDIDOS CANCELADOS\n\n" + historialCancelados);
    }

    public void asignacion() {
        if (pedidosPendientes.esVacia() || repartidores.esVacia()) {
            return;
        }
        Pedido pedido = pedidosPendientes.getPrimero().getDato();
        Repartidor primero = repartidores.getPrimero().getDato();
        if (pedido.getProductosElegidos().esVacia()) {
            pedidosPendientes.desencolar();
            return;
        }
        if (primero.isDisponibilidad()) {
            if (primero.getZona().equals(pedido.getUsuario().getZona())) {
                repartidores.desencolar();
            } else {
                for (int i = 0; i <= repartidores.getTamanio(); i++) {
                    Repartidor aux = repartidores.getPrimero().getDato();
                    repartidores.desencolar();
                    if (aux.getZona().equals(pedido.getUsuario().getZona())) {
                        primero = aux;
                        continue;
                    }
                    repartidores.encolar(aux, aux.isDisponibilidad() ? 1 : 0);
                }
                if (primero == repartidores.getPrimero().getDato()) {
                    repartidores.desencolar();
                }
            }

            pedido.setRappi(primero);
            pedido.setEstadoActual(EstadoPedido.REPARTIDOR_ASIGNADO);
            int domi = calcularDomicilio(pedido);
            pedido.setValorDomicilio(domi);
            pedido.setValorTotal(domi + pedido.getValorDeProducto());

            pedidoParaEntregar.encolar(pedido);
            pedidosPendientes.desencolar();

            primero.setDisponibilidad(false);
            repartidores.encolar(primero, primero.isDisponibilidad() ? 1 : 0);
            System.out.println(primero.getNombre() + " fue asignado a tu pedido" + pedido.getCodigo());
        }
    }

    public void finalizarPedido(Pedido repartidor) {
        if (repartidor == null || repartidor.getRappi() == null) {
            System.out.println("El pedido no tiene repartidor asignado");
            return;
        }
        repartidor.getRappi().setDisponibilidad(true);
        for (int i = 0; i < repartidores.getTamanio(); i++) {
            Repartidor aux = repartidores.getPrimero().getDato();
            repartidores.desencolar();

            if (aux.getId() != repartidor.getRappi().getId()) {
                repartidores.encolar(aux, 0);
            }
        }
        repartidores.encolar(repartidor.getRappi(), repartidor.getRappi().isDisponibilidad() ? 1 : 0);

        repartidor.setEstadoActual(EstadoPedido.ENTREGADO);
        historialEntregas.apilar(repartidor);
        repartidor.getUsuario().getHistorialPedidos().apilar(repartidor);
        repartidor.getRappi().getHistorialPedidos().apilar(repartidor);
        repartidor.getRappi().setSaldo(repartidor.getRappi().getSaldo() + 5000);
        double valor = repartidor.getValorDomicilio() - 5000;
        double f = 0;
        if (valor >= 0) {
            f = valor * 0.40;
        }
        repartidor.getRappi().setSaldo(repartidor.getRappi().getSaldo() + f);
        saldo += valor - f;

        for (int i = 0; i < pedidoParaEntregar.getTamanio(); i++) {
            Pedido aux = pedidoParaEntregar.getPrimero().getDato();
            pedidoParaEntregar.desencolar();
            if (aux.getCodigo() != repartidor.getCodigo()) {
                pedidoParaEntregar.encolar(aux);
            }
        }
        System.out.println("Pedido " + repartidor.getCodigo() + " entregado correctamente");
    }

    public int calcularDomicilio(Pedido pedido) {
        double valorDomicilio;
        double tarifa = 5000;
        double costoZona, costoCantidad, costoHorario, costoPrioridad = 0, costoRepartidor;
        //para Zona
        String zU = pedido.getUsuario().getZona(), zT = pedido.getTienda().getZona();
        if (zT.equals(zU)) {
            costoZona = 0;
            pedido.setTiempoEstimado(10);
        } else if ((zT.equalsIgnoreCase("Norte") && zU.equalsIgnoreCase("Sur"))
                || (zT.equalsIgnoreCase("Sur") && zU.equalsIgnoreCase("Norte"))
                || (zT.equalsIgnoreCase("Este") && zU.equalsIgnoreCase("Oeste"))
                || (zT.equalsIgnoreCase("Oeste") && zU.equalsIgnoreCase("Este"))) {
            costoZona = 4000;
            pedido.setTiempoEstimado(20);
        } else {
            costoZona = 2000;
            pedido.setTiempoEstimado(15);
        }
        //para cantidad
        int cantidadTotal = 0;
        Nodo<Producto> actual = pedido.getProductosElegidos().getPrimero();
        while (actual != null) {
            Producto p = actual.getDato();
            cantidadTotal += p.getCantidad();
            actual = actual.getSiguiente();
        }
        if (cantidadTotal < 6) {
            costoCantidad = 0;
        } else if (cantidadTotal < 11) {
            costoCantidad = 1000;
        } else if (cantidadTotal < 21) {
            costoCantidad = 2000;
        } else {
            costoCantidad = 3000;
        }
        //para hora
        String fecha = pedido.getFechaHora();
        int hora = Integer.parseInt(fecha.split(" ")[2].split(":")[0]);
        if (hora <= 6) {
            costoHorario = 3000;
        } else if (hora <= 18) {
            costoHorario = 0;
        } else {
            costoHorario = 2000;
        }
        //si el cliente es vip
        String dia = fecha.split(" ")[0];
        Cliente usuario = pedido.getUsuario();
        if (pedido.getValorDeProducto() > 200000) {
            costoPrioridad += 0.15;
        }
        if (pedido.getUsuario().getHistorialPedidos().esVacia()) {
            costoPrioridad += 0.1;
        }
        if (usuario.isVip()) {
            if (dia.equals("sabado") || dia.equals("domingo")) {
                costoPrioridad = 1;
            } else {
            costoPrioridad += 0.2;
            }
        } else {
            costoPrioridad += 0;
        }
        //la calificacion del repartidor
        double r = pedido.getRappi().getCalificacion();

        if (r < 3) {
            costoRepartidor = 0;
        } else if (r < 4.5) {
            costoRepartidor = 1000;
        } else {
            costoRepartidor = 2000;
        }

        // calculo total
        System.out.println(costoPrioridad);
        double suma = pedido.getUsuario().getPenalizacion() + costoZona + costoCantidad + costoHorario + costoRepartidor + tarifa;
        valorDomicilio = suma - (suma * costoPrioridad);
        return (int) valorDomicilio;
    }

    public void cancelarPedido(Pedido pedido) {
        if (pedido == null) {
            return;
        }
        EstadoPedido estado = pedido.getEstadoActual();
        if (estado == EstadoPedido.CANCELADO) {
            System.out.println("El pedido " + pedido.getCodigo() + " ya esta cancelado.");
            return;
        }
        long pago = 0;
        if (estado == EstadoPedido.ENTREGADO) {
            System.out.println("No se puede cancelar el pedido " + pedido.getCodigo());
            return;
        }
        if (null == estado) {
            System.out.println("Pedido " + pedido.getCodigo() + " cancelado correctamente");
        } else {
            switch (estado) {
                case REPARTIDOR_ASIGNADO -> {
                    System.out.println("Pedido " + pedido.getCodigo() + " cancelado. Penalizacion: $2000");
                    pago = 2000;
                }
                case EN_CAMINO -> {
                    System.out.println("Pedido " + pedido.getCodigo() + " cancelado. Penalizacion: $5000");
                    pago = 5000;
                }
                default ->
                    System.out.println("Pedido " + pedido.getCodigo() + " cancelado correctamente");
            }
        }
        Repartidor r = pedido.getRappi();
        if (r != null) {
            r.setDisponibilidad(true);
            int tamaño = repartidores.getTamanio();
            for (int i = 0; i < tamaño; i++) {
                Repartidor aux = repartidores.getPrimero().getDato();
                repartidores.desencolar();
                if (aux.getId() != r.getId()) {
                    repartidores.encolar(aux, 0);
                }
            }
            repartidores.encolar(r, 1);
        }
        // devolver productos al inventario
        Nodo actual = pedido.getProductosElegidos().getPrimero();
        while (actual != null) {
            Producto p = (Producto) actual.getDato();
            Producto original = pedido.getTienda().buscarProducto(p.getCodigoProducto());
            if (original != null) {
                original.setCantidad(original.getCantidad() + p.getCantidad());
            }
            actual = actual.getSiguiente();
        }
        if (pedido.getEstadoActual() == EstadoPedido.REPARTIDOR_ASIGNADO) {
            for (int i = 0; i < pedidoParaEntregar.getTamanio(); i++) {
                Pedido aux = pedidoParaEntregar.getPrimero().getDato();
                pedidoParaEntregar.desencolar();
                if (aux.getCodigo() != pedido.getCodigo()) {
                    pedidoParaEntregar.encolar(aux);
                }
            }
        } else if (pedido.getEstadoActual() == EstadoPedido.ESPERANDO_REPARTIDOR) {
            int tamanioOriginal = pedidosPendientes.getTamanio();

            for (int i = 0; i < tamanioOriginal; i++) {
                Pedido aux = pedidosPendientes.getPrimero().getDato();
                pedidosPendientes.desencolar();

                if (aux.getCodigo() != pedido.getCodigo()) {
                    pedidosPendientes.encolar(aux, 0);
                }
            }
        }
        pedidosCancelados++;
        pedido.setEstadoActual(EstadoPedido.CANCELADO);
        
        // Agregar al historial del cliente y penalizar
        if (pedido.getUsuario() != null) {
            pedido.getUsuario().getHistorialPedidos().apilar(pedido);
            pedido.getUsuario().setPenalizacion(pago);
        }
        
        // Si tiene repartidor asignado, agregar a su historial
        if (pedido.getRappi() != null) {
            pedido.getRappi().getHistorialPedidos().apilar(pedido);
        }
        
        // Agregar al historial cancelados
        historialCancelados.apilar(pedido);
    }

    public void calificarPedido(Repartidor repartidor, double calificacion) {

        double suma = repartidor.getCalificacion() * repartidor.getCalificaciones();
        repartidor.setCalificaciones(repartidor.getCalificaciones() + 1);
        repartidor.setCalificacion((suma + calificacion) / repartidor.getCalificaciones());
    }

    public Pila<Pedido> getHistorialCancelados() {
        return historialCancelados;
    }

    public void setHistorialCancelados(Pila<Pedido> historialCancelados) {
        this.historialCancelados = historialCancelados;
    }

    public void avanzarEstado(Pedido pedido) {
        if (pedido.getEstadoActual() == EstadoPedido.REPARTIDOR_ASIGNADO) {
            pedido.setEstadoActual(EstadoPedido.EN_CAMINO);
            System.out.println("Pedido " + pedido.getCodigo() + " va en camino...");
        } else if (pedido.getEstadoActual() == EstadoPedido.EN_CAMINO) {
            finalizarPedido(pedido);
        }
    }

    public long getSaldo() {
        return saldo;
    }

    public void setSaldo(long saldo) {
        this.saldo = saldo;
    }

    public int getPedidosCancelados() {
        return pedidosCancelados;
    }

    public void setPedidosCancelados(int pedidosCancelados) {
        this.pedidosCancelados = pedidosCancelados;
    }

    public void mostrarEstadisticas() {
        System.out.println("\n----- ESTADISTICAS -----");
        System.out.println("Pedidos entregados: " + historialEntregas.getTamanio());
        System.out.println("Pedidos cancelados: " + pedidosCancelados);
        System.out.println("Ganancias: $" + saldo);
        Repartidor mejor = mejorRepartidor();
        if (mejor != null) {
            System.out.println("Repartidor estrella:\n" + mejor.getNombre() + " (" + mejor.getHistorialPedidos().getTamanio() + " pedidos)");
        }
        Cliente mejorc = mejorCliente();
        if (mejorc != null) {
            System.out.println("Cliente estrella: " + mejorc.getNombre() + " (" + mejorc.getHistorialPedidos().getTamanio() + " pedidos)");
        }
        System.out.println("Zona mas activa: " + zonaMasPedidos());
    }

    public Repartidor mejorRepartidor() {
        Repartidor mejor = null;
        int mayor = 0;
        for (int i = 0; i < repartidores.getTamanio(); i++) {
            Repartidor aux = repartidores.getPrimero().getDato();
            repartidores.desencolar();
            int pedidos = aux.getHistorialPedidos().getTamanio();
            if (pedidos > mayor) {
                mayor = pedidos;
                mejor = aux;
            }
            repartidores.encolar(aux, 0);
        }
        return mejor;
    }

    public Cliente mejorCliente() {
        Cliente mejor = null;
        int mayor = 0;
        for (Cliente aux : clientes.values()) {
            int pedidosValidos = 0;
            Pila<Pedido> historial = aux.getHistorialPedidos();
            Nodo actual = historial.getCima();
            while (actual != null) {
                Pedido p = (Pedido) actual.getDato();
                if (p.getEstadoActual() != EstadoPedido.CANCELADO) {
                    pedidosValidos++;
                }
                actual = actual.getSiguiente();
            }
            if (pedidosValidos > mayor) {
                mayor = pedidosValidos;
                mejor = aux;
            }
        }
        return mejor;
    }

    public String zonaMasPedidos() {
        int norte = 0;
        int sur = 0;
        int este = 0;
        int oeste = 0;
        Nodo actual = historialEntregas.getCima();
        while (actual != null) {
            Pedido p = (Pedido) actual.getDato();
            String zona = p.getUsuario().getZona();
            switch (zona) {
                case "Norte" ->
                    norte++;
                case "Sur" ->
                    sur++;
                case "Este" ->
                    este++;
                case "Oeste" ->
                    oeste++;
            }
            actual = actual.getSiguiente();
        }
        int mayor = norte;
        String zona = "Norte";
        if (sur > mayor) {
            mayor = sur;
            zona = "Sur";
        }
        if (este > mayor) {
            mayor = este;
            zona = "Este";
        }
        if (oeste > mayor) {
            mayor = oeste;
            zona = "Oeste";
        }
        return zona + " (" + mayor + " pedidos)";
    }

    public Pedido buscarPedido(int codigo) {
        return pedidosPorCodigo.get(codigo);
    }
}
