
package Model;

public class Repartidor extends Persona{
   
   private boolean disponibilidad;
   private double calificacion;
   private int calificaciones;
   private double saldo;
      private Pila<Pedido> historialPedidos;

    public Repartidor(double calificacion, double saldo, long id, String nombre, long telefono, String zona) {
        super(id, nombre, telefono, zona);
        this.disponibilidad = true;
        this.calificacion = calificacion;
        this.saldo = saldo;
        this.calificaciones = 1;
        this.historialPedidos=new Pila<>();
    }




    
    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public boolean isDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(boolean disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    public double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(double calificacion) {
        this.calificacion = calificacion;
    }

    @Override
    public String toString() {
        return "REPARTIDOR: " +super.getNombre()+ "\t" + (disponibilidad?"   Disponible":"   Ocupado   ") + "(" + calificacion + "+)   "+super.getZona();
    }

    public int getCalificaciones() {
        return calificaciones;
    }

    public void setCalificaciones(int calificaciones) {
        this.calificaciones = calificaciones;
    }

    public Pila<Pedido> getHistorialPedidos() {
        return historialPedidos;
    }

    public void setHistorialPedidos(Pila<Pedido> historialPedidos) {
        this.historialPedidos = historialPedidos;
    }
   
    
    
    
    
    
}
