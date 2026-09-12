package com.tecsup.controller;

import com.tecsup.model.*;
import com.tecsup.service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    @Autowired private PacienteService pacienteService;

    // RF-PAC-01 + RF-PAC-02: registrar paciente
    // POST /api/pacientes
    @PostMapping
    public ResponseEntity<?> registrarPaciente(@RequestBody Paciente paciente) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(pacienteService.registrarPaciente(paciente));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // RF-PAC-05: buscar — endpoints de INT-3
    @GetMapping("/documento/{numero}")
    public ResponseEntity<?> buscarPorDocumento(@PathVariable String numero) {
        return pacienteService.buscarPorDocumento(numero)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<?> buscarPorCodigo(@PathVariable String codigo) {
        return pacienteService.buscarPorCodigo(codigo)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/buscar")
    public ResponseEntity<List<Paciente>> buscar(
            @RequestParam(required=false) String nombres,
            @RequestParam(required=false) String apellidoPaterno,
            @RequestParam(required=false) String apellidoMaterno) {
        if (nombres != null) return ResponseEntity.ok(pacienteService.buscarPorNombres(nombres));
        if (apellidoPaterno != null) return ResponseEntity.ok(pacienteService.buscarPorApellidoPaterno(apellidoPaterno));
        if (apellidoMaterno != null) return ResponseEntity.ok(pacienteService.buscarPorApellidoMaterno(apellidoMaterno));
        return ResponseEntity.ok(pacienteService.listarTodos());
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPaciente(@PathVariable Integer id) {
        return pacienteService.obtenerPacienteCompleto(id)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping
    public ResponseEntity<List<Paciente>> listarTodos() {
        return ResponseEntity.ok(pacienteService.listarTodos());
    }

    // RF-PAC-08: actualizar — INT-4
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPaciente(@PathVariable Integer id,
                                                @RequestBody Paciente datos, @RequestParam Integer idUsuario) {
        try { return ResponseEntity.ok(pacienteService.actualizarPaciente(id, datos, idUsuario)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    // RF-PAC-13: eliminar lógico
    // DELETE /api/pacientes/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPaciente(@PathVariable Integer id) {
        try {
            pacienteService.eliminarPaciente(id);
            return ResponseEntity.ok("Paciente procesado correctamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // RF-PAC-12: bitácora — INT-6
    @GetMapping("/{id}/bitacora")
    public ResponseEntity<List<BitacoraPaciente>> obtenerBitacora(@PathVariable Integer id) {
        return ResponseEntity.ok(pacienteService.obtenerBitacora(id));
    }
}