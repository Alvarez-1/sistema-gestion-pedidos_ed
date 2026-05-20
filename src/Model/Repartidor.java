/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Logica;

/**
 *
 * @author Usuario
 */
public class Repartidor extends Persona{
   
   private boolean disponibilidad;
   private double calificacion;
   private long saldo;

    public Repartidor(boolean disponibilidad, double calificacion, long saldo, long id, String nombre, long telefono, String zona) {
        super(id, nombre, telefono, zona);
        this.disponibilidad = disponibilidad;
        this.calificacion = calificacion;
        this.saldo = saldo;
    }

    public long getSaldo() {
        return saldo;
    }

    public void setSaldo(long saldo) {
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
        return "Repartidor{" + "disponibilidad=" + disponibilidad + ", calificacion=" + calificacion + ", saldo=" + saldo + '}';
    }
   
    
    
    
    
    
}
