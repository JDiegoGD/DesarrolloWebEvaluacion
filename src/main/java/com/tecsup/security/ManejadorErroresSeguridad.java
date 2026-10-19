package com.tecsup.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CsrfException;
import java.io.IOException;

// /api/** -> JSON 401/403; vistas -> /login (401) o /acceso-denegado (403)
public class ManejadorErroresSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final AuthenticationEntryPoint login = new LoginUrlAuthenticationEntryPoint("/login");
    private final AccessDeniedHandlerImpl paginaAccesoDenegado = new AccessDeniedHandlerImpl();

    public ManejadorErroresSeguridad() {
        paginaAccesoDenegado.setErrorPage("/acceso-denegado");
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException, ServletException {
        if (esApi(request))
            escribirJson(response, HttpServletResponse.SC_UNAUTHORIZED, "No autenticado",
                    "Debes iniciar sesión para acceder a este recurso.", request);
        else
            login.commence(request, response, ex);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException, ServletException {
        if (!esApi(request)) {
            paginaAccesoDenegado.handle(request, response, ex);
            return;
        }
        String mensaje = ex instanceof CsrfException
                ? "Token CSRF ausente o inválido. Recarga la página e inténtalo de nuevo."
                : "No tienes permiso para realizar esta operación.";
        escribirJson(response, HttpServletResponse.SC_FORBIDDEN, "Acceso denegado", mensaje, request);
    }

    public static boolean esApi(HttpServletRequest request) {
        return request.getRequestURI().startsWith(request.getContextPath() + "/api/");
    }

    public static void escribirJson(HttpServletResponse response, int estado, String error, String mensaje,
                                    HttpServletRequest request) throws IOException {
        response.setStatus(estado);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"estado\":" + estado
                + ",\"error\":\"" + escapar(error)
                + "\",\"mensaje\":\"" + escapar(mensaje)
                + "\",\"ruta\":\"" + escapar(request.getRequestURI()) + "\"}");
    }

    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
