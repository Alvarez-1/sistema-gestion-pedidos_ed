
package Logica;

public class NewMain {

    public static void main(String[] args) {
        
        ListaSimple<Producto> P1=new ListaSimple<>();
        
        for (int i = 0; i < 10; i++) {
        P1.insertarUltimo(new Producto(i, "carro ", 10000, 1));
    
        }
        
     Comercio Consecionario = new Comercio(0, "carritos", "consecionario", "carre", "Norte", P1);
        System.out.println(Consecionario);   
        
        
        
    }
    
}
