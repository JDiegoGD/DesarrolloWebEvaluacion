package com.tecsup.dto;

import com.tecsup.model.Auditoria;
import java.time.LocalDateTime;

public record AuditoriaDTO(
        Long id,
        String usuario,
        Integer idUsuario,
        String rolUsuario,
        LocalDateTime fechaHora,
        String operacion,
        String entidad,
        String idRegistro,
        String detalle
) {
    public static AuditoriaDTO de(Auditoria a) {
        return new AuditoriaDTO(a.getIdAuditoria(), a.getUsuario(), a.getIdUsuario(), a.getRolUsuario(),
                a.getFechaHora(), a.getOperacion().name(), a.getEntidad(), a.getIdRegistro(), a.getDetalle());
    }
}
