package com.tecsup.exception;

// Regla de negocio que choca con el estado actual (duplicados, último administrador...): 409
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
