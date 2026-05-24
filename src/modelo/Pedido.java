package modelo;

import estructura.ListaSimple;
import estructura.Nodo;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Pedido {

    private int codigo;
    private Cliente usuario;
    private Repartidor rappi;
    private Comercio tienda;
    private ListaSimple<Producto> productosElegidos;
    private double valorDeProducto;
    private double valorDomicilio;
    private double valorTotal;
    private EstadoPedido estadoActual;
    private String fecha;
    private int tiempoEstimado;

    public Pedido(int codigo, Cliente usuario, Comercio tienda) {
        this.codigo = codigo;
        this.usuario = usuario;
        this.tienda = tienda;
        this.valorDeProducto = 0;
        this.valorDomicilio = 0;
        this.valorTotal = 0;
        this.fecha = fechaHora;
        this.estadoActual = EstadoPedido.RECIBIDO;
        this.productosElegidos = new ListaSimple<>();
        this.tiempoEstimado = 0;
    }

    public Comercio getTienda() {
        return tienda;
    }

    public void setTienda(Comercio tienda) {
        this.tienda = tienda;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Cliente getUsuario() {
        return usuario;
    }

    public void setUsuario(Cliente usuario) {
        this.usuario = usuario;
    }

    public Repartidor getRappi() {
        return rappi;
    }

    public void setRappi(Repartidor rappi) {
        this.rappi = rappi;
    }

    public ListaSimple<Producto> getProductosElegidos() {
        return productosElegidos;
    }

    public void setProductosElegidos(ListaSimple<Producto> productosElegidos) {
        this.productosElegidos = productosElegidos;
    }

    public double getValorDeProducto() {
        return valorDeProducto;
    }

    public void setValorDeProducto(double valorDeProducto) {
        this.valorDeProducto = valorDeProducto;
    }

    public double getValorDomicilio() {
        return valorDomicilio;
    }

    public void setValorDomicilio(double valorDomicilio) {
        this.valorDomicilio = valorDomicilio;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public EstadoPedido getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(EstadoPedido estadoActual) {
        this.estadoActual = estadoActual;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    LocalDateTime ahora = LocalDateTime.now();
    DateTimeFormatter formato = DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy HH:mm:ss");
    String fechaHora = ahora.format(formato);

    public String getFechaHora() {
        return fechaHora;
    }

    @Override
    public String toString() {
        return "PEDIDO " + codigo
                + "\n" + fecha
                + "\n" + usuario
                + "" + tienda.getNombre().toUpperCase()
                + "\nproductosElegidos:\n" + productosElegidos.mostrar(1)
                + "valorProducto:\t" + valorDeProducto
                + "\nvalorDomicilio:\t" + valorDomicilio
                + "\nvalor Total:\t" + valorTotal
                + "\nPedido " + estadoActual
                + "\n" 
                + ((rappi != null) ? "REPARTIDOR: " + rappi.getNombre() + "   (" + rappi.getCalificacion() + " +) " + rappi.getZona() + "\nTiempoEstimado: " + tiempoEstimado + " minutos" : "")
                + "\n";
    }

    public void agregarProducto(int codigoProducto, int cantidadElegida) {
        Producto nuevo = tienda.buscarProducto(codigoProducto);
        if (nuevo != null && nuevo.getCantidad() >= cantidadElegida) {
            if (productosElegidos.buscarCodigo(codigoProducto)) {
                Nodo actual = productosElegidos.getPrimero();
                while (actual != null) {
                    Producto p = (Producto) actual.getDato();
                    if (p.getCodigoProducto() == codigoProducto && nuevo.getCantidad() >= cantidadElegida) {
                        p.setCantidad(p.getCantidad() + cantidadElegida);
                        p.setPrecio(p.getPrecio() + (nuevo.getPrecio() * cantidadElegida));
                        break;
                    }
                    actual = actual.getSiguiente();
                }
            } else {
                if (nuevo.getCantidad() >= cantidadElegida) {
                Producto elegido = new Producto(nuevo.getCodigoProducto(), nuevo.getNombre(), nuevo.getPrecio() * cantidadElegida, cantidadElegida);
                productosElegidos.insertarPrimero(elegido);
                }
            }
            valorDeProducto += ((int) nuevo.getPrecio() * cantidadElegida);
            nuevo.setCantidad(nuevo.getCantidad() - cantidadElegida);
            estadoActual = EstadoPedido.ESPERANDO_REPARTIDOR;
            System.out.println("Tu pedido fue recibido");
        }
    }
    
    public void eliminacionProducto(int codigoProducto, int cantidadElegida) {
        Producto nuevo = tienda.buscarProducto(codigoProducto);
        if (nuevo != null) {
            if (productosElegidos.buscarCodigo(codigoProducto)) {
                Nodo actual = productosElegidos.getPrimero();
                while (actual != null) {
                    Producto p = (Producto) actual.getDato();
                    if (p.getCodigoProducto() == codigoProducto && p.getCantidad() >= cantidadElegida) {
                        if (p.getCantidad() - cantidadElegida == 0) {
                            productosElegidos.eliminarPrimero();
                           valorDeProducto -= nuevo.getPrecio() * cantidadElegida;
                            break;
                        }
                        System.out.println("Producto eliminado");
                        p.setCantidad(p.getCantidad() - cantidadElegida);
                        p.setPrecio(p.getPrecio() - (nuevo.getPrecio() * cantidadElegida));
                        valorDeProducto -= nuevo.getPrecio() * cantidadElegida;
                        nuevo.setCantidad(nuevo.getCantidad() + cantidadElegida);
                        estadoActual = EstadoPedido.ESPERANDO_REPARTIDOR;
                        break;
                    } else {
                    actual = actual.getSiguiente();
                    }
                }
            }
        }
    }

    public int getTiempoEstimado() {
        return tiempoEstimado;
    }

    public void setTiempoEstimado(int tiempoEstimado) {
        this.tiempoEstimado = tiempoEstimado;
    }

    public boolean tieneProductos() {
        return productosElegidos != null && productosElegidos.getPrimero() != null;
    }
    
}
