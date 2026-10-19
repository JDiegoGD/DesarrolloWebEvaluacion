package com.tecsup.dto;

// Resultado de eliminar un paciente: baja lógica (pasa a Inactivo, tiene atenciones) o eliminación física
public record EliminacionPaciente(
        Integer idPaciente,
        String numeroDocumento,
        boolean bajaLogica
) {}
