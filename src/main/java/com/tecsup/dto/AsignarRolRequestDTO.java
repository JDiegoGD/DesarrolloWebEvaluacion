package com.tecsup.dto;

import jakarta.validation.constraints.NotNull;

public record AsignarRolRequestDTO(
        @NotNull(message = "el rol es obligatorio (rolId)")
        Integer rolId
) {}
