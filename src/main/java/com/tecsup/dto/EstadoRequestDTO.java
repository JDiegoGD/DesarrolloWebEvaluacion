package com.tecsup.dto;

import jakarta.validation.constraints.NotNull;

public record EstadoRequestDTO(
        @NotNull(message = "debes indicar el estado (activo: true o false)")
        Boolean activo
) {}
