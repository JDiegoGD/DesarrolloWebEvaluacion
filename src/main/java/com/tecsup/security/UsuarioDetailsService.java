package com.tecsup.security;

import com.tecsup.model.Usuario;
import com.tecsup.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Authority "ROLE_" + rol.nombre; habilitado solo si el usuario y su rol están activos
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String nombreUsuario) throws UsernameNotFoundException {
        Usuario u = usuarioRepository.findByNombreUsuarioIgnoreCase(nombreUsuario == null ? "" : nombreUsuario.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));
        return User.withUsername(u.getNombreUsuario())
                .password(u.getPassword())
                .roles(u.getRol().getNombre())
                .disabled(!(u.isActivo() && u.getRol().isActivo()))
                .build();
    }
}
