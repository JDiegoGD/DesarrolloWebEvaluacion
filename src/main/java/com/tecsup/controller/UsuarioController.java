package com.tecsup.controller;

import com.tecsup.model.Usuario;
import com.tecsup.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired private UsuarioService service;

    @PostMapping
    public ResponseEntity<Usuario> registrar(@Valid @RequestBody Usuario u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(u));
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listar(
            @RequestParam(required = false) String rol) {
        if (rol != null) return ResponseEntity.ok(service.listarPorRol(rol));
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{nombreUsuario}")
    public ResponseEntity<?> obtener(@PathVariable String nombreUsuario) {
        return service.obtenerPorNombreUsuario(nombreUsuario)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{nombreUsuario}")
    public ResponseEntity<?> eliminar(@PathVariable String nombreUsuario) {
        try {
            service.eliminar(nombreUsuario);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}