package Model;


public class Pila<T> {

    private Nodo<T> cima;
    private int tamanio;

    public Pila() {
        this.cima = null;
        this.tamanio = 0;
    }

    public Nodo<T> getCima() {
        return cima;
    }

    public void setCima(Nodo<T> cima) {
        this.cima = cima;
    }

    public int getTamanio() {
        return tamanio;
    }

    public void setTamanio(int tamanio) {
        this.tamanio = tamanio;
    }

    public boolean esVacia() {
        return cima == null && tamanio == 0;
    }

    public void apilar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato, cima);
        cima = nuevo;
        tamanio++;
    }

    public void desapilar() {
        if (!esVacia()) {
            cima = cima.getSiguiente();
            tamanio--;
        }
    }

    public String mostrar() {
        StringBuilder pila = new StringBuilder();
        Nodo<T> actual = cima;
        while (actual != null) {
            pila.append(actual.getDato()).append("\n");
            actual = actual.getSiguiente();
        }
        return pila.toString();
    }
    
    public Pila<T> invertirPila(){
    Pila<T> pilaAux = new Pila<>();
    while(cima!=null){
    pilaAux.apilar(cima.getDato());
    desapilar();
    }
    return pilaAux;
    }

    @Override
    public String toString() {
        return mostrar();
    }
    

}
