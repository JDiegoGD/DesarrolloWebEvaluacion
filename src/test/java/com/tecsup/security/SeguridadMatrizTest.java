package com.tecsup.security;

import com.tecsup.controller.PacienteController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Matriz de permisos de SecurityConfig. Arranca el contexto completo (necesita la BD de XAMPP),
 * pero no escribe datos: las peticiones permitidas son GET y las escrituras probadas se deniegan
 * antes de llegar al controlador. UsuarioDetailsService se sustituye para que UsuarioVigenteFilter
 * reconozca a los usuarios de @WithMockUser como activos.
 */
@SpringBootTest
class SeguridadMatrizTest {

    private static final String DOC = "00000000";
    private static final Map<String, String> ROL_POR_USUARIO =
            Map.of("admin", "ADMINISTRADOR", "medico", "MEDICO", "recepcion", "RECEPCIONISTA");

    @Autowired private WebApplicationContext context;
    @Autowired private PacienteController pacienteController;
    @MockitoBean private UsuarioDetailsService usuarioDetailsService;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        when(usuarioDetailsService.loadUserByUsername(anyString())).thenAnswer(inv -> {
            String nombre = inv.getArgument(0);
            return User.withUsername(nombre).password("x").roles(ROL_POR_USUARIO.getOrDefault(nombre, "NINGUNO")).build();
        });
    }

    // --- Sin sesión
    @Test
    void anonimo_apiDevuelve401Json_vistaRedirigeALogin() throws Exception {
        mvc.perform(get("/api/pacientes"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mensaje").value("Debes iniciar sesión para acceder a este recurso."));
        mvc.perform(get("/pacientes"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(get("/login")).andExpect(status().isOk());
    }

    // --- ADMINISTRADOR
    @Test
    @WithMockUser(username = "admin", roles = "ADMINISTRADOR")
    void administrador_accedeAAdministracionYSinCsrfRecibe403() throws Exception {
        mvc.perform(get("/api/usuarios")).andExpect(status().isOk());
        mvc.perform(get("/api/roles")).andExpect(status().isOk());
        mvc.perform(get("/usuarios")).andExpect(status().isOk());
        mvc.perform(get("/bitacora")).andExpect(status().isOk());
        // Escritura sin token CSRF
        mvc.perform(post("/api/catalogos/tipo-documento").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Token CSRF ausente o inválido. Recarga la página e inténtalo de nuevo."));
    }

    // --- MEDICO
    @Test
    @WithMockUser(username = "medico", roles = "MEDICO")
    void medico_historiaClinicaSi_administracionYEscrituraDeSegurosNo() throws Exception {
        mvc.perform(get("/api/pacientes")).andExpect(status().isOk());
        mvc.perform(get("/api/atenciones")).andExpect(status().isOk());
        mvc.perform(get("/api/seguros/paciente/" + DOC)).andExpect(status().isOk());
        mvc.perform(get("/pacientes")).andExpect(status().isOk());

        mvc.perform(get("/api/usuarios")).andExpect(status().isForbidden());
        mvc.perform(get("/api/roles")).andExpect(status().isForbidden());
        mvc.perform(post("/api/seguros").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/pacientes/documento/" + DOC).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/api/catalogos/tipo-documento").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        // Vista sin permiso: página propia /acceso-denegado con 403
        mvc.perform(get("/usuarios"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/acceso-denegado"));
    }

    // --- RECEPCIONISTA
    @Test
    @WithMockUser(username = "recepcion", roles = "RECEPCIONISTA")
    void recepcionista_seguroYContactosSi_historiaClinicaYBitacoraNo() throws Exception {
        mvc.perform(get("/api/pacientes")).andExpect(status().isOk());
        mvc.perform(get("/api/atenciones")).andExpect(status().isOk());
        mvc.perform(get("/api/contactos-emergencia/paciente/" + DOC)).andExpect(status().isOk());
        mvc.perform(get("/seguros")).andExpect(status().isOk());

        mvc.perform(get("/api/pacientes/documento/" + DOC + "/alergias")).andExpect(status().isForbidden());
        mvc.perform(get("/api/pacientes/documento/" + DOC + "/antecedentes-personales")).andExpect(status().isForbidden());
        mvc.perform(get("/api/pacientes/documento/" + DOC + "/antecedentes-familiares")).andExpect(status().isForbidden());
        mvc.perform(get("/api/pacientes/documento/" + DOC + "/bitacora")).andExpect(status().isForbidden());
        mvc.perform(get("/api/usuarios")).andExpect(status().isForbidden());
        mvc.perform(get("/bitacora")).andExpect(status().isForbidden());
    }

    // --- @PreAuthorize: el permiso no depende solo de la URL
    @Test
    @WithMockUser(username = "medico", roles = "MEDICO")
    void preAuthorize_bloqueaBorradoDePacienteAunqueSeLlameAlControladorDirectamente() {
        assertThatThrownBy(() -> pacienteController.eliminarPaciente(DOC))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(username = "recepcion", roles = "RECEPCIONISTA")
    void preAuthorize_bloqueaHistoriaClinicaAunqueSeLlameAlControladorDirectamente() {
        assertThatThrownBy(() -> pacienteController.listarAlergias(DOC))
                .isInstanceOf(AccessDeniedException.class);
    }
}
