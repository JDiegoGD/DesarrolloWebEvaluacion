package com.tecsup.controller;

import com.tecsup.model.SeguroPaciente;
import com.tecsup.service.SeguroPacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/seguros")
public class SeguroPacienteController {

    @Autowired private SeguroPacienteService service;

    @PostMapping
    public ResponseEntity<SeguroPaciente> registrar(@RequestBody SeguroPaciente s) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(s));
    }

    @GetMapping("/paciente/{id}")
    public ResponseEntity<List<SeguroPaciente>> listar(@PathVariable Integer id) {
        return ResponseEntity.ok(service.listarPorPaciente(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id,
                                        @RequestBody SeguroPaciente datos) {
        try { return ResponseEntity.ok(service.actualizar(id, datos)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}