package com.tecsup.auditoria;

import com.tecsup.security.UsuarioDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// /api/auditoria y /auditoria: solo ADMINISTRADOR. Solo lecturas: no escribe en la BD
@SpringBootTest
class AuditoriaAccesoTest {

    private static final Map<String, String> ROL_POR_USUARIO =
            Map.of("admin", "ADMINISTRADOR", "medico", "MEDICO", "recepcion", "RECEPCIONISTA");

    @Autowired private WebApplicationContext context;
    @MockitoBean private UsuarioDetailsService usuarioDetailsService;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        // UsuarioVigenteFilter revalida al usuario en cada petición
        when(usuarioDetailsService.loadUserByUsername(anyString())).thenAnswer(inv -> {
            String nombre = inv.getArgument(0);
            return User.withUsername(nombre).password("x").roles(ROL_POR_USUARIO.getOrDefault(nombre, "NINGUNO")).build();
        });
    }

    @Test
    void sinSesion_401() throws Exception {
        mvc.perform(get("/api/auditoria")).andExpect(status().isUnauthorized());
        mvc.perform(get("/auditoria")).andExpect(status().isFound()).andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "medico", roles = "MEDICO")
    void medico_403() throws Exception {
        mvc.perform(get("/api/auditoria")).andExpect(status().isForbidden());
        mvc.perform(get("/api/auditoria/entidades")).andExpect(status().isForbidden());
        mvc.perform(get("/auditoria")).andExpect(status().isForbidden()).andExpect(forwardedUrl("/acceso-denegado"));
    }

    @Test
    @WithMockUser(username = "recepcion", roles = "RECEPCIONISTA")
    void recepcionista_403() throws Exception {
        mvc.perform(get("/api/auditoria")).andExpect(status().isForbidden());
        mvc.perform(get("/api/auditoria/operaciones")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMINISTRADOR")
    void administrador_200_yValidaParametros() throws Exception {
        mvc.perform(get("/api/auditoria").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tamanio").value(5))
                .andExpect(jsonPath("$.contenido").isArray());
        mvc.perform(get("/api/auditoria/operaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("REGISTRO"));
        mvc.perform(get("/auditoria")).andExpect(status().isOk());
        mvc.perform(get("/api/auditoria").param("size", "101")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/auditoria").param("operacion", "BORRADO")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/auditoria").param("desde", "2026-13-01")).andExpect(status().isBadRequest());
    }
}
