package com.tecsup.dto;

// cantidadUsuarios: usuarios asignados al rol (activos e inactivos)
public record RolResponseDTO(
        Integer id,
        String nombre,
        String descripcion,
        Boolean activo,
        Long cantidadUsuarios
) {}
