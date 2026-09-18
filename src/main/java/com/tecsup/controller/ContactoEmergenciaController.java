package com.tecsup.controller;

import com.tecsup.model.ContactoEmergencia;
import com.tecsup.service.ContactoEmergenciaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/contactos-emergencia")
public class ContactoEmergenciaController {

    @Autowired private ContactoEmergenciaService service;

    @PostMapping
    public ResponseEntity<?> registrar(@Valid @RequestBody ContactoEmergencia c) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(c));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/paciente/{numeroDocumento}")
    public ResponseEntity<List<ContactoEmergencia>> listar(@PathVariable String numeroDocumento) {
        return ResponseEntity.ok(service.listarPorPaciente(numeroDocumento));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id,
                                        @RequestBody ContactoEmergencia datos) {
        try { return ResponseEntity.ok(service.actualizar(id, datos)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}