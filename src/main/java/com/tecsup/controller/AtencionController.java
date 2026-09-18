package com.tecsup.controller;

import com.tecsup.model.Atencion;
import com.tecsup.service.AtencionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/atenciones")
public class AtencionController {

    @Autowired private AtencionService service;

    // RF-PAC-07: resumen de atenciones por paciente
    @GetMapping("/paciente/{numeroDocumento}")
    public ResponseEntity<List<Atencion>> listarPorPaciente(
            @PathVariable String numeroDocumento) {
        return ResponseEntity.ok(service.listarPorPaciente(numeroDocumento));
    }

    @PostMapping
    public ResponseEntity<?> registrar(@Valid @RequestBody Atencion a) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(a));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Atencion>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}