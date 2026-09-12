package com.tecsup.controller;

import com.tecsup.model.Atencion;
import com.tecsup.service.AtencionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/atenciones")
public class AtencionController {

    @Autowired private AtencionService service;

    // RF-PAC-07: resumen de atenciones por paciente
    // GET /api/atenciones/paciente/{id}
    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<List<Atencion>> listarPorPaciente(
            @PathVariable Integer idPaciente) {
        return ResponseEntity.ok(service.listarPorPaciente(idPaciente));
    }

    @PostMapping
    public ResponseEntity<Atencion> registrar(@RequestBody Atencion a) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(a));
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