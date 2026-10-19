package com.tecsup.exception;

// Se traduce a 404 en GlobalExceptionHandler
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}