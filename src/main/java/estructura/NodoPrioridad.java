package estructura;

public class NodoPrioridad<T> {
    private T dato;
    private int prioridad;
    private NodoPrioridad<T> siguiente;

    public NodoPrioridad(T dato, int prioridad) {
        this.dato = dato;
        this.prioridad = prioridad;
        this.siguiente = null;
    }

    public T getDato() {
        return dato;
    }

    public void setDato(T dato) {
        this.dato = dato;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public NodoPrioridad<T> getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodoPrioridad<T> siguiente) {
        this.siguiente = siguiente;
    }

    @Override
    public String toString() {
        return "NodoPrioridad{" + "dato=" + dato + ", prioridad=" + prioridad + ", siguiente=" + siguiente + '}';
    }
    
    
}
