package com.tecsup.controller;

import com.tecsup.dto.EstadoRequestDTO;
import com.tecsup.dto.RolRequestDTO;
import com.tecsup.dto.RolResponseDTO;
import com.tecsup.service.RolService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// Sin DELETE: los roles se desactivan. Errores (400/404/409) en GlobalExceptionHandler
@RestController
@RequestMapping("/api/roles")
public class RolController {

    @Autowired private RolService service;

    @PostMapping
    public ResponseEntity<RolResponseDTO> crear(@Valid @RequestBody RolRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @GetMapping
    public ResponseEntity<List<RolResponseDTO>> listar(@RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(service.listar(activo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolResponseDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RolResponseDTO> actualizar(@PathVariable Integer id, @Valid @RequestBody RolRequestDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<RolResponseDTO> cambiarEstado(@PathVariable Integer id,
                                                        @Valid @RequestBody EstadoRequestDTO dto) {
        return ResponseEntity.ok(service.cambiarEstado(id, dto.activo()));
    }
}
