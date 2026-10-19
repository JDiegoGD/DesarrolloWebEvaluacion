package com.tecsup.exception;

// Datos válidos en forma pero no aceptables (rol inactivo, médico sin rol MEDICO...): 400
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
