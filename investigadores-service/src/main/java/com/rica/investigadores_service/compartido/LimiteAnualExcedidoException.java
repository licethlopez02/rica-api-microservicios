package com.rica.investigadores_service.compartido;

public class LimiteAnualExcedidoException extends RuntimeException {
    public LimiteAnualExcedidoException(String message) {
        super(message);
    }
}