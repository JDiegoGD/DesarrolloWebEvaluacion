package com.tecsup.auditoria;

import com.tecsup.model.Paciente;
import com.tecsup.repository.AuditoriaRepository;
import com.tecsup.service.PacienteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Un fallo al guardar la auditoría (repositorio que lanza excepción) no rompe la operación de negocio
@SpringBootTest
@WithMockUser(username = "admin", roles = "ADMINISTRADOR")
class AuditoriaFalloGuardadoTest {

    @Autowired private PacienteService pacienteService;
    @Autowired private JdbcTemplate jdbc;
    @MockitoBean private AuditoriaRepository auditoriaRepository;

    @AfterEach
    void limpiar() {
        jdbc.update("DELETE FROM paciente WHERE numero_documento LIKE 'T4-%'");
    }

    @Test
    void errorAlGuardarAuditoria_noAfectaLaOperacion() {
        when(auditoriaRepository.save(any())).thenThrow(new IllegalStateException("fallo simulado de auditoría"));

        Paciente p = pacienteService.registrarPaciente(AuditoriaIntegracionTest.nuevoPaciente());

        assertThat(p.getIdPaciente()).isNotNull();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM paciente WHERE numero_documento = ?",
                Integer.class, p.getNumeroDocumento())).isEqualTo(1);
        verify(auditoriaRepository).save(any());   // se intentó auditar y el error se absorbió
    }
}
