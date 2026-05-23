package modelo;

import estructura.Pila;

public class Cliente extends Persona {

   private String direccion;   
   private boolean vip;
   private Pila<Pedido> historialPedidos;
   private double penalizacion;

    public Cliente(String direccion, boolean vip, long id, String nombre, long telefono, String zona) {
        super(id, nombre, telefono, zona);
        this.direccion = direccion;
        this.vip = vip;
        this.historialPedidos = new Pila<>();
        this.penalizacion = 0;
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
        return "Cliente: " + super.getNombre() + " " + (vip ? "PREMIUM" : "") + "\ndireccion: " + direccion + "\tzona: " + super.getZona() + "\n";
    }

    public Pila<Pedido> getHistorialPedidos() {
        return historialPedidos;
    }

    public void setHistorialPedidos(Pila<Pedido> historialPedidos) {
        this.historialPedidos = historialPedidos;
    }

    public double getPenalizacion() {
        return penalizacion;
    }

    public void setPenalizacion(double penalizacion) {
        this.penalizacion = penalizacion;
    }




    
}
