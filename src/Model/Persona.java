
package Logica;





public class Persona {
 
   private long id;
   private String nombre;
   private long telefono;
   private String zona;

    @Override
    public String toString() {
        return "Persona{" + "id=" + id + ", nombre=" + nombre + ", telefono=" + telefono + ", zona=" + zona + '}';
    }

    public Persona(long id, String nombre, long telefono, String zona) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.zona = zona;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public long getTelefono() {
        return telefono;
    }

    public void setTelefono(long telefono) {
        this.telefono = telefono;
    }
    
    
}
