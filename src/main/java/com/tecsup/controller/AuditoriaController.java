package com.tecsup.controller;

import com.tecsup.dto.AuditoriaPaginaDTO;
import com.tecsup.service.AuditoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// Solo lectura y solo ADMINISTRADOR (también en SecurityConfig). Errores 400 en GlobalExceptionHandler
@RestController
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService service;

    public AuditoriaController(AuditoriaService service) {
        this.service = service;
    }

    // Fechas ISO: 2026-10-01 o 2026-10-01T00:00:00. Orden: fecha descendente. size máximo 100
    @GetMapping
    public ResponseEntity<AuditoriaPaginaDTO> consultar(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) String operacion,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.consultar(usuario, entidad, operacion, desde, hasta, page, size));
    }

    @GetMapping("/entidades")
    public ResponseEntity<List<String>> entidades() {
        return ResponseEntity.ok(service.entidades());
    }

    @GetMapping("/operaciones")
    public ResponseEntity<List<String>> operaciones() {
        return ResponseEntity.ok(service.operaciones());
    }
}
