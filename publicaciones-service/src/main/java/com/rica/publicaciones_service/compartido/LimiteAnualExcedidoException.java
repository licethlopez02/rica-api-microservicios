package com.rica.publicaciones_service.compartido;

public class LimiteAnualExcedidoException extends RuntimeException {
    public LimiteAnualExcedidoException(String message) {
        super(message);
    }
}