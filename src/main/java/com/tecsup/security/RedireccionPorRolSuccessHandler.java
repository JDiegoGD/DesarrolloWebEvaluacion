package com.tecsup.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.access.WebInvocationPrivilegeEvaluator;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// Vuelve a la URL protegida solicitada si el rol tiene permiso; si no, al destino de su rol
public class RedireccionPorRolSuccessHandler implements AuthenticationSuccessHandler {

    private static final Map<String, String> DESTINO_POR_ROL = Map.of(
            "ROLE_ADMINISTRADOR", "/usuarios",
            "ROLE_MEDICO", "/pacientes",
            "ROLE_RECEPCIONISTA", "/pacientes");

    private final RequestCache requestCache;
    // Se resuelve en cada login: el evaluador se construye a partir de la propia cadena de filtros
    private final ObjectProvider<WebInvocationPrivilegeEvaluator> evaluador;

    public RedireccionPorRolSuccessHandler(RequestCache requestCache,
                                           ObjectProvider<WebInvocationPrivilegeEvaluator> evaluador) {
        this.requestCache = requestCache;
        this.evaluador = evaluador;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String contexto = request.getContextPath();
        String destino = contexto + destinoPorRol(authentication);

        SavedRequest guardada = requestCache.getRequest(request, response);
        if (guardada != null) {
            requestCache.removeRequest(request, response);
            String ruta = URI.create(guardada.getRedirectUrl()).getPath().substring(contexto.length());
            WebInvocationPrivilegeEvaluator ev = evaluador.getIfAvailable();
            if (ev != null && ev.isAllowed(contexto, ruta, "GET", authentication))
                destino = guardada.getRedirectUrl();
        }

        HttpSession sesion = request.getSession(false);
        if (sesion != null) sesion.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        response.sendRedirect(destino);
    }

    static String destinoPorRol(Authentication authentication) {
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        return DESTINO_POR_ROL.entrySet().stream()
                .filter(e -> roles.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst().orElse("/");
    }
}
