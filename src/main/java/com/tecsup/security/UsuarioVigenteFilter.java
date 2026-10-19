package com.tecsup.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * La Authentication guardada en sesión no se vuelve a validar: si un administrador desactiva a
 * un usuario (o a su rol) o le cambia el rol, la sesión abierta seguiría funcionando. Este filtro
 * recarga el usuario en cada petición autenticada (una consulta por petición) y, si ya no está
 * habilitado o sus roles cambiaron, invalida la sesión y rechaza la petición.
 */
public class UsuarioVigenteFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;

    public UsuarioVigenteFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // Recursos estáticos: públicos, no hace falta consultar la BD
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        return ruta.startsWith("/css/") || ruta.startsWith("/js/") || ruta.startsWith("/images/")
                || ruta.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            chain.doFilter(request, response);
            return;
        }

        UserDetails actual;
        try {
            actual = userDetailsService.loadUserByUsername(auth.getName());
        } catch (UsernameNotFoundException e) {
            actual = null;
        }

        if (actual == null || !actual.isEnabled()) {
            cerrarSesion(request);
            rechazar(request, response, "/login?desactivado",
                    "Usuario desactivado. Tu sesión se cerró; contacta al administrador.");
            return;
        }
        if (!roles(actual.getAuthorities()).equals(roles(auth.getAuthorities()))) {
            cerrarSesion(request);
            rechazar(request, response, "/login?expirado",
                    "Tus permisos cambiaron. Vuelve a iniciar sesión.");
            return;
        }
        chain.doFilter(request, response);
    }

    private void cerrarSesion(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession sesion = request.getSession(false);
        if (sesion != null) sesion.invalidate();
    }

    private void rechazar(HttpServletRequest request, HttpServletResponse response,
                          String urlLogin, String mensaje) throws IOException {
        if (ManejadorErroresSeguridad.esApi(request))
            ManejadorErroresSeguridad.escribirJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "No autenticado", mensaje, request);
        else
            response.sendRedirect(request.getContextPath() + urlLogin);
    }

    // Solo ROLE_*: Spring Security 7 añade al autenticar authorities de factor (FACTOR_PASSWORD)
    // que no están en el UserDetails
    private static Set<String> roles(java.util.Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream().map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .collect(Collectors.toSet());
    }
}
