package bot.sesion;

import estructura.ListaSimple;
import modelo.Comercio;
import modelo.Pedido;
import modelo.Producto;

public class SesionPedidoTemporal {
    private Comercio comercioSeleccionado;
    private Pedido pedidoTemporal;
    private Producto productoSeleccionado;
    private ListaSimple<Comercio> comerciosDisponibles;
    private ListaSimple<Producto> productosDisponibles;

    public Comercio getComercioSeleccionado() {
        return comercioSeleccionado;
    }

    public void setComercioSeleccionado(Comercio comercioSeleccionado) {
        this.comercioSeleccionado = comercioSeleccionado;
    }

    public Pedido getPedidoTemporal() {
        return pedidoTemporal;
    }

    public void setPedidoTemporal(Pedido pedidoTemporal) {
        this.pedidoTemporal = pedidoTemporal;
    }

    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }

    public void setProductoSeleccionado(Producto productoSeleccionado) {
        this.productoSeleccionado = productoSeleccionado;
    }

    public ListaSimple<Comercio> getComerciosDisponibles() {
        return comerciosDisponibles;
    }

    public void setComerciosDisponibles(ListaSimple<Comercio> comerciosDisponibles) {
        this.comerciosDisponibles = comerciosDisponibles;
    }

    public ListaSimple<Producto> getProductosDisponibles() {
        return productosDisponibles;
    }

    public void setProductosDisponibles(ListaSimple<Producto> productosDisponibles) {
        this.productosDisponibles = productosDisponibles;
    }
}
