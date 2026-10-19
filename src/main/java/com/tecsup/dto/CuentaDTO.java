package com.tecsup.dto;

import java.util.List;

// permisos: vistas a las que el usuario tiene acceso según la matriz de SecurityConfig
public record CuentaDTO(
        String nombreUsuario,
        String nombres,
        String rol,
        List<String> permisos
) {}
