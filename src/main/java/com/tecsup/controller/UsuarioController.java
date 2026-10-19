package com.tecsup.controller;

import com.tecsup.dto.AsignarRolRequestDTO;
import com.tecsup.dto.EstadoRequestDTO;
import com.tecsup.dto.UsuarioRequestDTO;
import com.tecsup.dto.UsuarioResponseDTO;
import com.tecsup.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// Errores (400/404/409) los resuelve GlobalExceptionHandler
@RestController
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired private UsuarioService service;

    // Ahora recibe UsuarioRequestDTO (password, nombres y rolId); el formato antiguo {nombreUsuario, rol} ya no aplica
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> registrar(
            @Validated({Default.class, UsuarioRequestDTO.Crear.class}) @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    // ?rol= (nombre del rol) se conserva por compatibilidad con el filtro anterior
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar(
            @RequestParam(required = false) Integer rolId,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(service.listar(rolId, rol, activo, q));
    }

    // Las dos regex son excluyentes (solo dígitos / al menos un no dígito); con solo \d+ Spring
    // considera ambigua la ruta. Por eso un nombreUsuario no puede ser solo números (UsuarioRequestDTO)
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @GetMapping("/{nombreUsuario:.*\\D.*}")
    public ResponseEntity<?> obtener(@PathVariable String nombreUsuario) {
        return service.obtenerPorNombreUsuario(nombreUsuario)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<UsuarioResponseDTO> actualizar(@PathVariable Integer id,
                                                         @Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @PatchMapping("/{id:\\d+}/estado")
    public ResponseEntity<UsuarioResponseDTO> cambiarEstado(@PathVariable Integer id,
                                                            @Valid @RequestBody EstadoRequestDTO dto) {
        return ResponseEntity.ok(service.cambiarEstado(id, dto.activo()));
    }

    @PatchMapping("/{id:\\d+}/rol")
    public ResponseEntity<UsuarioResponseDTO> cambiarRol(@PathVariable Integer id,
                                                         @Valid @RequestBody AsignarRolRequestDTO dto) {
        return ResponseEntity.ok(service.cambiarRol(id, dto.rolId()));
    }

    @DeleteMapping("/{nombreUsuario}")
    public ResponseEntity<Void> eliminar(@PathVariable String nombreUsuario) {
        service.eliminar(nombreUsuario);
        return ResponseEntity.noContent().build();
    }
}
