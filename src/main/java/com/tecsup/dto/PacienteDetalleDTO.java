package com.tecsup.dto;

import java.time.LocalDate;

public record PacienteDetalleDTO(
        Long id,
        String codigoPaciente,
        String tipoDocumento,
        String numeroDocumento,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        LocalDate fechaNacimiento,
        Integer edad,
        String sexo,
        String estadoCivil,
        String tipoSangre,
        String telefono,
        String correo,
        String direccion,
        String distrito,
        String provincia,
        String departamento,
        String ocupacion,
        String estado
) {}