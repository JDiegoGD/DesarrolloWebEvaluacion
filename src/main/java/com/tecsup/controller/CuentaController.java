package com.tecsup.controller;

import com.tecsup.dto.CambioPasswordDTO;
import com.tecsup.dto.CuentaDTO;
import com.tecsup.model.Usuario;
import com.tecsup.service.UsuarioActualService;
import com.tecsup.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.WebInvocationPrivilegeEvaluator;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// Cuenta del usuario autenticado (cualquier rol)
@RestController
@RequestMapping("/api/cuenta")
public class CuentaController {

    private static final List<String> VISTAS =
            List.of("pacientes", "atenciones", "seguros", "contactos", "bitacora", "usuarios", "roles", "auditoria");

    private final UsuarioActualService usuarioActualService;
    private final UsuarioService usuarioService;
    private final ObjectProvider<WebInvocationPrivilegeEvaluator> evaluador;

    public CuentaController(UsuarioActualService usuarioActualService, UsuarioService usuarioService,
                            ObjectProvider<WebInvocationPrivilegeEvaluator> evaluador) {
        this.usuarioActualService = usuarioActualService;
        this.usuarioService = usuarioService;
        this.evaluador = evaluador;
    }

    @GetMapping("/me")
    public ResponseEntity<CuentaDTO> me(Authentication authentication) {
        Usuario u = usuarioActualService.obtener();
        WebInvocationPrivilegeEvaluator ev = evaluador.getObject();
        List<String> permisos = VISTAS.stream()
                .filter(vista -> ev.isAllowed("/" + vista, authentication))
                .toList();
        return ResponseEntity.ok(new CuentaDTO(u.getNombreUsuario(), u.getNombres(), u.getRol().getNombre(), permisos));
    }

    @PostMapping("/password")
    public ResponseEntity<String> cambiarPassword(@Valid @RequestBody CambioPasswordDTO dto) {
        usuarioService.cambiarPasswordPropia(usuarioActualService.nombreUsuario(), dto.passwordActual(), dto.passwordNueva());
        return ResponseEntity.ok("Contraseña actualizada correctamente.");
    }
}
