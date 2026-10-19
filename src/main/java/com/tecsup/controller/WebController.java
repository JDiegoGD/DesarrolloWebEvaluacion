package com.tecsup.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

// Los permisos de cada vista están en SecurityConfig
@Controller
public class WebController {

    @GetMapping("/login")
    public String login(Authentication authentication) {
        boolean autenticado = authentication != null && !(authentication instanceof AnonymousAuthenticationToken);
        return autenticado ? "redirect:/" : "login";
    }

    // Cualquier método: AccessDeniedHandlerImpl hace forward conservando el método original
    @RequestMapping("/acceso-denegado")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String accesoDenegado() { return "acceso-denegado"; }

    @GetMapping("/")
    public String index() { return "index"; }

    @GetMapping("/pacientes")
    public String pacientes() { return "pacientes"; }

    @GetMapping("/atenciones")
    public String atenciones() { return "atenciones"; }

    @GetMapping("/seguros")
    public String seguros() { return "seguros"; }

    @GetMapping("/contactos")
    public String contactos() { return "contactos"; }

    @GetMapping("/usuarios")
    public String usuarios() { return "usuarios"; }

    @GetMapping("/bitacora")
    public String bitacora() { return "bitacora"; }

    @GetMapping("/auditoria")
    public String auditoria() { return "auditoria"; }
}
