package estructura;

import modelo.Producto;

public class ListaSimple<T> {

    private Nodo primero;
    private Nodo ultimo;
    private int tamanio;

    public ListaSimple() {
        this.primero = null;
        this.ultimo = null;
        this.tamanio = 0;
    }

    public ListaSimple(Nodo primero, Nodo ultimo, int tamanio) {
        this.primero = primero;
        this.ultimo = ultimo;
        this.tamanio = tamanio;
    }

    public Nodo getPrimero() {
        return primero;
    }

    public void setPrimero(Nodo primero) {
        this.primero = primero;
    }

    public Nodo getUltimo() {
        return ultimo;
    }

    public void setUltimo(Nodo ultimo) {
        this.ultimo = ultimo;
    }

    public int getTamanio() {
        return tamanio;
    }

    public void setTamanio(int tamanio) {
        this.tamanio = tamanio;
    }

    @Override
    public String toString() {
        return "Lista{" + "primero=" + primero + ", ultimo=" + ultimo + ", tamanio=" + tamanio + '}';
    }

    //metodos
    public boolean esVacia() {
        return this.primero == null && this.ultimo == null && this.tamanio == 0;
    }

    public String mostrar() {
        StringBuilder lista = new StringBuilder();
        Nodo<T> actual = primero;
        if (!esVacia()) {
            while (actual != null) {
                lista.append(actual.getDato()).append("\n");
                actual = actual.getSiguiente();
            }
        } else {
            lista.append("Lista vacia");
        }

        return lista.toString();
    }

    public String mostrar(int tipo) {
        StringBuilder lista = new StringBuilder();
        Nodo<T> actual = primero;
        while (actual != null) {
            lista.append(((Producto) actual.getDato()).toString(tipo))
                    .append("\n");
            actual = actual.getSiguiente();
        }
        return lista.toString();
    }

    public void insertarPrimero(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato, primero);
        if (esVacia()) {
            this.ultimo = nuevo;
        }
        primero = nuevo;
        tamanio++;
    }

    public void insertarUltimo(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato, null);
        if (esVacia()) {
            this.primero = this.ultimo = nuevo;
        } else {
            this.ultimo.setSiguiente(nuevo);
            this.ultimo = nuevo;
        }
        tamanio++;
    }

    public void eliminarPrimero() {
        if (!esVacia()) {
            if (primero == ultimo) {
                primero = ultimo = null;
            } else {
                primero = primero.getSiguiente();
            }
            tamanio--;
        }
    }

    public void eliminarUltimo() {
        if (!esVacia()) {
            if (primero == ultimo) {
                primero = ultimo = null;
            } else {
                Nodo<T> actual = primero;
                while (actual.getSiguiente() != ultimo) {
                    actual = actual.getSiguiente();
                }
                ultimo = actual;
                ultimo.setSiguiente(null);
            }
            tamanio--;
        }
    }

    public boolean buscar(T dato) {
        if (!esVacia()) {
            Nodo<T> actual = primero;
            while (actual != null) {
                if (actual.getDato() == dato) {
                    return true;
                }
                actual = actual.getSiguiente();
            }
        }
        return false;
    }

    public boolean buscarCodigo(int codigo) {
        Nodo<Producto> actual = primero;
        while (actual != null) {
            Producto p = actual.getDato();
            if (p.getCodigoProducto() == codigo) {
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false;
    }

    public void eliminarDato(T dato) {
        if (!esVacia()) {
            if (dato.equals(primero.getDato())) {
                eliminarPrimero();
            } else if (dato.equals(ultimo.getDato())) {
                eliminarUltimo();
            } else {
                Nodo<T> actual = primero;
                while (!(actual.getSiguiente() == null) && !actual.getSiguiente().getDato().equals(dato)) {
                    actual = actual.getSiguiente();
                }
                if (!actual.equals(ultimo)) {
                    actual.setSiguiente(actual.getSiguiente().getSiguiente());
                    tamanio--;
                }
            }
        }
    }

}
