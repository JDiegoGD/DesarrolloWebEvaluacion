package com.tecsup.dto;

public record PacienteBusquedaDTO(
        Long id,
        String codigoPaciente,
        String tipoDocumento,
        String numeroDocumento,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        String telefono,
        String estado
) {}