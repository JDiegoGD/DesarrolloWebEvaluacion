package com.tecsup.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

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
}