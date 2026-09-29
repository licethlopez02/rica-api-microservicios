package com.rica.investigadores_service.compartido;

public class RecursoNoEncontradoException extends RuntimeException{

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
