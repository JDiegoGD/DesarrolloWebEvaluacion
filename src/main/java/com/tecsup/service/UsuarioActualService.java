package com.tecsup.service;

import com.tecsup.exception.RecursoNoEncontradoException;
import com.tecsup.model.Usuario;
import com.tecsup.repository.UsuarioRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Única fuente de "quién hace la operación": la sesión, nunca un parámetro de la petición
@Service
public class UsuarioActualService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioActualService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String nombreUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken)
            throw new AuthenticationCredentialsNotFoundException("No hay un usuario autenticado.");
        return auth.getName();
    }

    // Usuario con su rol cargado
    @Transactional(readOnly = true)
    public Usuario obtener() {
        String nombre = nombreUsuario();
        return usuarioRepository.findByNombreUsuario(nombre)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado no encontrado: " + nombre));
    }
}
