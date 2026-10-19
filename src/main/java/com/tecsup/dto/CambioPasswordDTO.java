package com.tecsup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambioPasswordDTO(
        @NotBlank(message = "la contraseña actual es obligatoria")
        String passwordActual,

        @NotBlank(message = "la contraseña nueva es obligatoria")
        @Size(min = 8, max = 72, message = "la contraseña nueva debe tener entre 8 y 72 caracteres")
        String passwordNueva
) {}
