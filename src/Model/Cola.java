package Model;

public class Cola<T> {

    private Nodo<T> primero;
    private Nodo<T> ultimo;
    private int tamanio;

    public Cola() {
        this.primero = null;
        this.ultimo = null;
        this.tamanio = 0;
    }

    public int getTamanio() {
        return tamanio;
    }

    public void setTamanio(int tamanio) {
        this.tamanio = tamanio;
    }

    public Nodo<T> getPrimero() {
        return primero;
    }

    public void setPrimero(Nodo<T> primero) {
        this.primero = primero;
    }

    public Nodo<T> getUltimo() {
        return ultimo;
    }

    public void setUltimo(Nodo<T> ultimo) {
        this.ultimo = ultimo;
    }

    @Override
    public String toString() {
        return mostrarCola();
    }

    public boolean esVacia() {
        return primero == null && ultimo == null && tamanio == 0;
    }

    public void encolar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (esVacia()) {
            primero = nuevo;
        } else {
            ultimo.setSiguiente(nuevo);
        }
        ultimo = nuevo;
        tamanio++;
    }

    public void desencolar(){
        if (!esVacia()) {
            if (primero==ultimo) {
                primero=ultimo=null;
            }else{
            primero = primero.getSiguiente();
            }
        tamanio--;
        }
    }
    
    public String mostrarCola(){
    StringBuilder cola= new StringBuilder();
    Nodo<T> actual=primero;
    while(actual!=null){
    cola.append(actual.getDato()).append("\n");
    actual=actual.getSiguiente();
    }
    return cola.toString();
    }
    
    
}
