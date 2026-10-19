package com.tecsup.dto;

import java.util.List;

public record AuditoriaPaginaDTO(
        List<AuditoriaDTO> contenido,
        int pagina,
        int tamanio,
        long totalElementos,
        int totalPaginas
) {}
