package com.tecsup.auditoria;

import com.tecsup.dto.EliminacionPaciente;
import com.tecsup.dto.UsuarioResponseDTO;
import com.tecsup.model.Atencion;
import com.tecsup.model.ContactoEmergencia;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Resolución genérica del idRegistro (sin Spring ni BD)
class AuditoriaAspectIdTest {

    @Test
    void valorSimpleDevuelto() {
        assertThat(AuditoriaAspect.idPorConvencion(15, "Usuario", new Object[]{"x"})).isEqualTo("15");
    }

    @Test
    void recordConId() {
        UsuarioResponseDTO dto = new UsuarioResponseDTO(7, "ana", "Ana", null, true, 2, "MEDICO");
        assertThat(AuditoriaAspect.idPorConvencion(dto, "Usuario", new Object[]{})).isEqualTo("7");
    }

    @Test
    void recordConIdDeLaEntidad() {
        EliminacionPaciente r = new EliminacionPaciente(42, "70000001", true);
        assertThat(AuditoriaAspect.idPorConvencion(r, "Paciente", new Object[]{"70000001"})).isEqualTo("42");
    }

    @Test
    void entidadConVariosGetId_prefiereElDeLaEntidad() {
        Atencion a = new Atencion();
        a.setIdAtencion(9);
        a.setIdMedico(3);   // getIdMedico no debe confundirse con el id de la atención
        assertThat(AuditoriaAspect.idPorConvencion(a, "Atencion", new Object[]{})).isEqualTo("9");
    }

    @Test
    void getIdConOtroNombre() {
        ContactoEmergencia c = new ContactoEmergencia();
        c.setIdContacto(5);
        assertThat(AuditoriaAspect.idPorConvencion(c, "ContactoEmergencia", new Object[]{})).isEqualTo("5");
    }

    @Test
    void sinResultado_usaElPrimerArgumentoSimple() {
        assertThat(AuditoriaAspect.idPorConvencion(null, "ContactoEmergencia", new Object[]{12})).isEqualTo("12");
        assertThat(AuditoriaAspect.idPorConvencion(null, "Paciente", new Object[]{new Object(), "70000001"}))
                .isEqualTo("70000001");
    }
}
