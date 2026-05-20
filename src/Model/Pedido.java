

package Logica;



public class Pedido {
private int codigo;
private Cliente usuario;
private Repartidor rappi;
private Comercio tienda;
private ListaSimple<Producto> productosElegidos; 
private int valorDeProducto = 0;
private int valorDomicilio=0;
private int valorTotal=0;
private String estadoActual;
private String fecha;

    public Pedido(int codigo, Cliente usuario, Repartidor rappi, Comercio tienda) {
        this.codigo = codigo;
        this.usuario = usuario;
        this.rappi = rappi;
        this.tienda = tienda;
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

    public int getValorDeProducto() {
        return valorDeProducto;
    }

    public void setValorDeProducto(int valorDeProducto) {
        this.valorDeProducto = valorDeProducto;
    }

    public int getValorDomicilio() {
        return valorDomicilio;
    }

    public void setValorDomicilio(int valorDomicilio) {
        this.valorDomicilio = valorDomicilio;
    }

    public int getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(int valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }






    
}
