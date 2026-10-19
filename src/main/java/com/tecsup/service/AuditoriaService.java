package com.tecsup.service;

import com.tecsup.dto.AuditoriaDTO;
import com.tecsup.dto.AuditoriaPaginaDTO;
import com.tecsup.enums.OperacionAuditoria;
import com.tecsup.exception.SolicitudInvalidaException;
import com.tecsup.model.Auditoria;
import com.tecsup.repository.AuditoriaRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    /** Entidades auditadas (para los filtros del frontend aunque aún no tengan filas). */
    public static final List<String> ENTIDADES = List.of("Alergia", "AntecedenteFamiliar", "AntecedentePersonal",
            "Atencion", "ContactoEmergencia", "Paciente", "Rol", "SeguroPaciente", "Usuario");

    private static final int TAMANIO_MAXIMO = 100;

    private final AuditoriaRepository repository;
    private final TransactionTemplate transaccionNueva;

    public AuditoriaService(AuditoriaRepository repository, PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.transaccionNueva = new TransactionTemplate(transactionManager);
        this.transaccionNueva.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * Registra una fila SOLO cuando la operación de negocio ya está confirmada: si hay una transacción
     * en curso se espera a su commit (si hace rollback no se registra nada); si no, se guarda ya.
     * Se guarda en una transacción propia (REQUIRES_NEW) y cualquier error se escribe en el log sin
     * propagarse: un fallo al auditar nunca rompe ni revierte la operación principal.
     */
    public void registrar(Auditoria auditoria) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    guardar(auditoria);
                }
            });
        } else {
            guardar(auditoria);
        }
    }

    private void guardar(Auditoria auditoria) {
        try {
            transaccionNueva.executeWithoutResult(estado -> repository.save(auditoria));
        } catch (RuntimeException e) {
            log.error("No se pudo registrar la auditoría [{} {} {} por {}]: {}", auditoria.getOperacion(),
                    auditoria.getEntidad(), auditoria.getIdRegistro(), auditoria.getUsuario(), e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------- Consulta

    @Transactional(readOnly = true)
    public AuditoriaPaginaDTO consultar(String usuario, String entidad, String operacion,
                                        String desde, String hasta, int pagina, int tamanio) {
        if (pagina < 0) throw new SolicitudInvalidaException("El número de página no puede ser negativo.");
        if (tamanio < 1 || tamanio > TAMANIO_MAXIMO)
            throw new SolicitudInvalidaException("El tamaño de página debe estar entre 1 y " + TAMANIO_MAXIMO + ".");

        OperacionAuditoria op = parsearOperacion(operacion);
        LocalDateTime inicio = parsearFecha(desde, "desde", false);
        LocalDateTime fin = parsearFecha(hasta, "hasta", true);
        if (inicio != null && fin != null && inicio.isAfter(fin))
            throw new SolicitudInvalidaException("La fecha 'desde' no puede ser posterior a 'hasta'.");

        String usuarioFiltro = vacioANull(usuario);
        String entidadFiltro = vacioANull(entidad);
        Specification<Auditoria> filtros = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (usuarioFiltro != null)
                p.add(cb.like(cb.lower(root.get("usuario")), "%" + usuarioFiltro.toLowerCase(Locale.ROOT) + "%"));
            if (entidadFiltro != null) p.add(cb.equal(root.get("entidad"), entidadFiltro));
            if (op != null) p.add(cb.equal(root.get("operacion"), op));
            if (inicio != null) p.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), inicio));
            if (fin != null) p.add(cb.lessThanOrEqualTo(root.get("fechaHora"), fin));
            return cb.and(p.toArray(Predicate[]::new));
        };

        Sort orden = Sort.by(Sort.Direction.DESC, "fechaHora").and(Sort.by(Sort.Direction.DESC, "idAuditoria"));
        Page<Auditoria> resultado = repository.findAll(filtros, PageRequest.of(pagina, tamanio, orden));
        return new AuditoriaPaginaDTO(resultado.getContent().stream().map(AuditoriaDTO::de).toList(),
                resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(), resultado.getTotalPages());
    }

    @Transactional(readOnly = true)
    public List<String> entidades() {
        TreeSet<String> todas = new TreeSet<>(ENTIDADES);
        todas.addAll(repository.listarEntidades());
        return List.copyOf(todas);
    }

    public List<String> operaciones() {
        return Arrays.stream(OperacionAuditoria.values()).map(Enum::name).toList();
    }

    private static OperacionAuditoria parsearOperacion(String operacion) {
        String valor = vacioANull(operacion);
        if (valor == null) return null;
        try {
            return OperacionAuditoria.valueOf(valor.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new SolicitudInvalidaException("Operación no válida: '" + valor + "'. Valores permitidos: "
                    + String.join(", ", Arrays.stream(OperacionAuditoria.values()).map(Enum::name).toList()) + ".");
        }
    }

    // Acepta 2026-10-01 o 2026-10-01T00:00:00. Una fecha sin hora en 'hasta' incluye todo ese día.
    private static LocalDateTime parsearFecha(String texto, String campo, boolean finDelDia) {
        String valor = vacioANull(texto);
        if (valor == null) return null;
        try {
            if (valor.length() == 10) {
                LocalDate fecha = LocalDate.parse(valor);
                return finDelDia ? fecha.atTime(23, 59, 59, 999_999_999) : fecha.atStartOfDay();
            }
            return LocalDateTime.parse(valor);
        } catch (DateTimeParseException e) {
            throw new SolicitudInvalidaException("La fecha '" + campo + "' no tiene formato ISO "
                    + "(por ejemplo 2026-10-01 o 2026-10-01T00:00:00).");
        }
    }

    private static String vacioANull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
