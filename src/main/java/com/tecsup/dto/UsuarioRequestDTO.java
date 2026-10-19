package com.tecsup.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Las reglas del grupo Crear solo aplican en el POST (en el PUT el password es opcional)
public record UsuarioRequestDTO(
        @NotBlank(groups = Crear.class, message = "el nombre de usuario es obligatorio")
        @Size(min = 3, max = 100, message = "el nombre de usuario debe tener entre 3 y 100 caracteres")
        @Pattern(regexp = "^(?!\\d+$).*", message = "el nombre de usuario no puede estar formado solo por números")
        String nombreUsuario,

        @NotBlank(message = "los nombres son obligatorios")
        @Size(max = 150, message = "los nombres admiten como máximo 150 caracteres")
        String nombres,

        @Email(message = "el correo no tiene un formato válido")
        @Size(max = 150, message = "el correo admite como máximo 150 caracteres")
        String correo,

        // BCrypt solo usa los primeros 72 bytes
        @NotBlank(groups = Crear.class, message = "la contraseña es obligatoria al crear un usuario")
        @Size(min = 8, max = 72, message = "la contraseña debe tener entre 8 y 72 caracteres")
        String password,

        @NotNull(message = "el rol es obligatorio (rolId)")
        Integer rolId
) {
    public interface Crear {}
}
