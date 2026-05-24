package modelo;

public enum EstadoPedido {
    RECIBIDO,
    PREPARANDO,
    ESPERANDO_REPARTIDOR,
    REPARTIDOR_ASIGNADO,
    EN_CAMINO,
    ENTREGADO,
    CANCELADO
}
