package modelo;

import estructura.ListaSimple;
import estructura.Nodo;

public class Comercio {

    private int codigo;
    private String nombre;
    private String tipoNegocio;
    private String direccion;
    private String zona;
    private ListaSimple<Producto> productos;

    public Comercio(int codigo, String nombre, String tipoNegocio, String direccion, String zona, ListaSimple<Producto> productos) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipoNegocio = tipoNegocio;
        this.direccion = direccion;
        this.zona = zona;
        this.productos = productos;
    }

    public ListaSimple<Producto> getProductos() {
        return productos;
    }

    public void setProductos(ListaSimple<Producto> productos) {
        this.productos = productos;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoNegocio() {
        return tipoNegocio;
    }

    public void setTipoNegocio(String tipoNegocio) {
        this.tipoNegocio = tipoNegocio;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    @Override
    public String toString() {
        return nombre.toUpperCase() + "\nCodigo: " + codigo + "\ntipoNegocio: " + tipoNegocio + "\ndireccion:" + direccion + "\tzona:" + zona + "\nPRODUCTOS\n" + productos.mostrar();
    }

   public Producto buscarProducto(int codigo) {
        if (!productos.esVacia()) {
            Nodo actual = productos.getPrimero();
            while (actual != null) {
                if (actual.getDato() instanceof Producto && ((Producto) actual.getDato()).getCodigoProducto() == codigo) {
                    return (Producto) actual.getDato();
                }
                actual = actual.getSiguiente();
            }
        }
        return null;
    }
}
