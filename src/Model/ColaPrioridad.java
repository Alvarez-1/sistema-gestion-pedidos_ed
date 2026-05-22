package Model;

public class ColaPrioridad<T> {

    private NodoPrioridad<T> primero;
    private NodoPrioridad<T> ultimo;
    private int tamanio;

    public ColaPrioridad() {
        this.primero = null;
        this.ultimo = null;
        this.tamanio = 0;
    }

    public NodoPrioridad<T> getPrimero() {
        return primero;
    }

    public void setPrimero(NodoPrioridad<T> primero) {
        this.primero = primero;
    }

    public NodoPrioridad<T> getUltimo() {
        return ultimo;
    }

    public void setUltimo(NodoPrioridad<T> ultimo) {
        this.ultimo = ultimo;
    }

    public int getTamanio() {
        return tamanio;
    }

    public void setTamanio(int tamanio) {
        this.tamanio = tamanio;
    }

    public boolean esVacia() {
        return primero == null && ultimo == null && tamanio == 0;
    }

    public void encolar(T dato, int prioridad) {
        NodoPrioridad<T> nuevo = new NodoPrioridad<>(dato, prioridad);
        if (esVacia()) {
            primero = ultimo = nuevo;
        } else if (prioridad > primero.getPrioridad()) {
            nuevo.setSiguiente(primero);
            primero = nuevo;
        } else {
            NodoPrioridad<T> actual = primero;
            while (actual.getSiguiente() != null && actual.getSiguiente().getPrioridad() >= prioridad) {
                actual = actual.getSiguiente();
            }
            nuevo.setSiguiente(actual.getSiguiente());
            actual.setSiguiente(nuevo);
            if (nuevo.getSiguiente() == null) {
                ultimo = nuevo;
            }
        }
        tamanio++;
    }

    public void desencolar() {
        if (!esVacia()) {
            if (primero == ultimo) {
                primero = ultimo = null;
            } else {
                primero = primero.getSiguiente();
            }
            tamanio--;
        }
    }

    public String mostrarCola() {
        StringBuilder cola = new StringBuilder();
        NodoPrioridad<T> actual = primero;
        while (actual != null) {
            cola.append(actual.getDato())
                    .append("\n");
            actual = actual.getSiguiente();
        }
        return cola.toString();
    }

    @Override
    public String toString() {
        return mostrarCola();
    }
}
