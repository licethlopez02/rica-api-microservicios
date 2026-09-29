package com.rica.investigadores_service.investigadores.dominio;

public class CorreoDuplicadoException extends RuntimeException{

    public CorreoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
