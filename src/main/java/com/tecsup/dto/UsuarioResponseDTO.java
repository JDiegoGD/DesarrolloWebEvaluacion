package com.tecsup.dto;

import com.tecsup.model.Usuario;

// Nunca incluye el password
public record UsuarioResponseDTO(
        Integer id,
        String nombreUsuario,
        String nombres,
        String correo,
        Boolean activo,
        Integer rolId,
        String rolNombre
) {
    public static UsuarioResponseDTO de(Usuario u) {
        return new UsuarioResponseDTO(u.getIdUsuario(), u.getNombreUsuario(), u.getNombres(), u.getCorreo(),
                u.isActivo(), u.getRol().getIdRol(), u.getRol().getNombre());
    }
}
