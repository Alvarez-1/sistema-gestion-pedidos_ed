
package Logica;





public class Comercio {

int codigo;
String nombre;
String tipoNegocio;
String direccion;
String zona;
    ListaSimple<Producto> productos;

    public Comercio(int codigo, String nombre, String tipoNegocio, String direccion, String zona, ListaSimple<Producto> productos) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipoNegocio = tipoNegocio;
        this.direccion = direccion;
        this.zona = zona;
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

    public ListaSimple<Producto> getProductos() {
        return productos;
    }

    public void setProductos(ListaSimple<Producto> productos) {
        this.productos = productos;
    }

    @Override
    public String toString() {
        return "Comercio{" + "codigo=" + codigo + ", nombre=" + nombre + ", tipoNegocio=" + tipoNegocio + ", direccion=" + direccion + ", zona=" + zona + ", productos=" + productos + '}';
    }

  
    
    
    




    
}
