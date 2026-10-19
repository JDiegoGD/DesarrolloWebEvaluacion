package com.tecsup.auditoria;

import com.tecsup.dto.EliminacionPaciente;
import com.tecsup.enums.OperacionAuditoria;
import com.tecsup.model.Auditoria;
import com.tecsup.model.Paciente;
import com.tecsup.model.TipoDocumento;
import com.tecsup.repository.AuditoriaRepository;
import com.tecsup.service.PacienteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prueba de integración contra la BD de XAMPP (necesita el usuario 'admin' y el tipo de documento DNI,
 * creados por la semilla). Cada prueba usa documentos "T4-..." y los borra al terminar, junto con
 * sus filas de auditoría. No es @Transactional a propósito: la auditoría se escribe tras el commit.
 */
@SpringBootTest
@WithMockUser(username = "admin", roles = "ADMINISTRADOR")
class AuditoriaIntegracionTest {

    @Autowired private PacienteService pacienteService;
    @Autowired private AuditoriaRepository auditoriaRepository;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transactionManager;

    @AfterEach
    void limpiar() {
        jdbc.update("DELETE b FROM bitacora_paciente b JOIN paciente p ON p.id_paciente = b.id_paciente "
                + "WHERE p.numero_documento LIKE 'T4-%'");
        jdbc.update("DELETE FROM paciente WHERE numero_documento LIKE 'T4-%'");
        jdbc.update("DELETE FROM auditoria WHERE detalle LIKE '%T4-%'");
    }

    @Test
    void registrarModificarYEliminar_generanSusFilas() {
        Paciente p = pacienteService.registrarPaciente(nuevoPaciente());
        String id = p.getIdPaciente().toString();
        assertThat(filas(id)).extracting(Auditoria::getOperacion).containsExactly(OperacionAuditoria.REGISTRO);
        Auditoria registro = filas(id).get(0);
        assertThat(registro.getUsuario()).isEqualTo("admin");
        assertThat(registro.getRolUsuario()).isEqualTo("ADMINISTRADOR");
        assertThat(registro.getDetalle()).contains(p.getNumeroDocumento());

        Paciente cambios = new Paciente();
        cambios.setNombres("Nombre Cambiado");
        cambios.setApellidoPaterno(p.getApellidoPaterno());
        cambios.setApellidoMaterno(p.getApellidoMaterno());
        cambios.setEstado("Activo");
        pacienteService.actualizarPaciente(p.getNumeroDocumento(), cambios);
        assertThat(filas(id)).extracting(Auditoria::getOperacion)
                .containsExactly(OperacionAuditoria.REGISTRO, OperacionAuditoria.MODIFICACION);
        assertThat(filas(id).get(1).getDetalle()).contains("Campos modificados: nombres");

        // Otro paciente sin atenciones ni bitácora: eliminación física
        Paciente otro = pacienteService.registrarPaciente(nuevoPaciente());
        EliminacionPaciente r = pacienteService.eliminarPaciente(otro.getNumeroDocumento());
        assertThat(r.bajaLogica()).isFalse();
        List<Auditoria> filasOtro = filas(otro.getIdPaciente().toString());
        assertThat(filasOtro).extracting(Auditoria::getOperacion)
                .containsExactly(OperacionAuditoria.REGISTRO, OperacionAuditoria.ELIMINACION);
        assertThat(filasOtro.get(1).getDetalle()).contains("Eliminación física");
    }

    @Test
    void operacionQueFalla_noDejaFila() {
        Paciente p = pacienteService.registrarPaciente(nuevoPaciente());
        long antes = auditoriaRepository.count();

        Paciente duplicado = nuevoPaciente();
        duplicado.setNumeroDocumento(p.getNumeroDocumento());
        assertThatThrownBy(() -> pacienteService.registrarPaciente(duplicado)).isInstanceOf(RuntimeException.class);

        assertThat(auditoriaRepository.count()).isEqualTo(antes);
    }

    @Test
    void rollbackDeLaTransaccionExterna_noDejaFilaFalsa() {
        long antes = auditoriaRepository.count();
        Paciente p = nuevoPaciente();

        new TransactionTemplate(transactionManager).executeWithoutResult(estado -> {
            pacienteService.registrarPaciente(p);
            estado.setRollbackOnly();
        });

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM paciente WHERE numero_documento = ?",
                Integer.class, p.getNumeroDocumento())).isZero();
        assertThat(auditoriaRepository.count()).isEqualTo(antes);
    }

    @Test
    void commitQueFallaPorFk_noDejaFila() {
        // Con bitácora y sin atenciones: el DELETE físico falla al hacer commit (FK de bitacora_paciente)
        Paciente p = pacienteService.registrarPaciente(nuevoPaciente());
        Paciente cambios = new Paciente();
        cambios.setNombres("Con Bitacora");
        cambios.setApellidoPaterno(p.getApellidoPaterno());
        cambios.setApellidoMaterno(p.getApellidoMaterno());
        cambios.setEstado("Activo");
        pacienteService.actualizarPaciente(p.getNumeroDocumento(), cambios);
        long antes = auditoriaRepository.count();

        assertThatThrownBy(() -> pacienteService.eliminarPaciente(p.getNumeroDocumento()))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(auditoriaRepository.count()).isEqualTo(antes);
    }

    private List<Auditoria> filas(String idRegistro) {
        return auditoriaRepository.findAll().stream()
                .filter(a -> "Paciente".equals(a.getEntidad()) && idRegistro.equals(a.getIdRegistro()))
                .sorted((x, y) -> x.getIdAuditoria().compareTo(y.getIdAuditoria()))
                .toList();
    }

    static Paciente nuevoPaciente() {
        Paciente p = new Paciente();
        TipoDocumento dni = new TipoDocumento();
        dni.setCodigo("DNI");
        p.setTipoDocumento(dni);
        p.setNumeroDocumento("T4-" + ThreadLocalRandom.current().nextInt(10_000_000, 99_999_999));
        p.setNombres("Prueba");
        p.setApellidoPaterno("Auditoria");
        p.setApellidoMaterno("Integracion");
        p.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        p.setSexo("F");
        return p;
    }
}
