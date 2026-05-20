
package Logica;


public class Cliente extends Persona{

   private String direccion;   
   private boolean vip;

    public Cliente(String direccion, boolean vip, long id, String nombre, long telefono, String zona) {
        super(id, nombre, telefono, zona);
        this.direccion = direccion;
        this.vip = vip;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean vip) {
        this.vip = vip;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    @Override
    public String toString() {
        return "Cliente{" + "direccion=" + direccion + ", vip=" + vip + '}';
    }




    
}
