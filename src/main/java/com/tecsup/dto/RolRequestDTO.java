package com.tecsup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RolRequestDTO(
        @NotBlank(message = "el nombre del rol es obligatorio")
        @Size(max = 50, message = "el nombre del rol admite como máximo 50 caracteres")
        String nombre,

        @Size(max = 255, message = "la descripción admite como máximo 255 caracteres")
        String descripcion
) {}
