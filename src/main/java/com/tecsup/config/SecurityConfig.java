package com.tecsup.config;

import com.tecsup.security.ManejadorErroresSeguridad;
import com.tecsup.security.RedireccionPorRolSuccessHandler;
import com.tecsup.security.UsuarioDetailsService;
import com.tecsup.security.UsuarioVigenteFilter;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.WebInvocationPrivilegeEvaluator;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

/**
 * Matriz de permisos del sistema (único lugar donde se define por URL).
 * "Citas" no existe en el modelo: se tratan como equivalentes a "atenciones" (/atenciones, /api/atenciones/**).
 * Los controladores sensibles repiten la regla con @PreAuthorize (defensa en profundidad).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMINISTRADOR";
    private static final String MEDICO = "MEDICO";
    private static final String RECEPCION = "RECEPCIONISTA";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Primero se valida la contraseña y después el estado: así "Usuario desactivado" solo lo ve
    // quien conoce la contraseña (no revela qué cuentas existen o están desactivadas)
    @Bean
    public AuthenticationProvider authenticationProvider(UsuarioDetailsService usuarioDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setPreAuthenticationChecks(usuario -> { });
        provider.setPostAuthenticationChecks(new AccountStatusUserDetailsChecker());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   UsuarioDetailsService usuarioDetailsService,
                                                   ObjectProvider<WebInvocationPrivilegeEvaluator> evaluador)
            throws Exception {
        ManejadorErroresSeguridad errores = new ManejadorErroresSeguridad();

        // Solo se recuerda la URL de vistas (GET), nunca de /api/** ni de errores o estáticos
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null);
        requestCache.setRequestMatcher(request -> "GET".equals(request.getMethod())
                && !ManejadorErroresSeguridad.esApi(request)
                && !request.getRequestURI().startsWith(request.getContextPath() + "/error"));

        AuthenticationFailureHandler fallo = (request, response, ex) -> response.sendRedirect(
                request.getContextPath() + (ex instanceof DisabledException ? "/login?desactivado" : "/login?error"));

        // Fuerza la carga del token en cada petición para que la cookie XSRF-TOKEN siempre exista
        Filter cargarTokenCsrf = (request, response, chain) -> {
            CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (token != null) token.getToken();
            chain.doFilter(request, response);
        };

        http
            .authorizeHttpRequests(auth -> auth
                // Públicas
                .requestMatchers("/login", "/acceso-denegado", "/error",
                        "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                // Cualquier usuario autenticado
                .requestMatchers(HttpMethod.GET, "/").authenticated()
                .requestMatchers("/api/cuenta/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/catalogos/**").authenticated()

                // Vistas
                .requestMatchers("/usuarios", "/roles", "/auditoria").hasRole(ADMIN)
                .requestMatchers("/bitacora").hasAnyRole(ADMIN, MEDICO)
                .requestMatchers("/pacientes", "/atenciones").hasAnyRole(ADMIN, MEDICO, RECEPCION)
                .requestMatchers("/seguros", "/contactos").hasAnyRole(ADMIN, RECEPCION)

                // Administración
                .requestMatchers("/api/usuarios/**", "/api/roles/**", "/api/auditoria/**").hasRole(ADMIN)
                .requestMatchers("/api/catalogos/**").hasRole(ADMIN)

                // Historia clínica y bitácora de pacientes: sin RECEPCIONISTA
                .requestMatchers("/api/pacientes/documento/*/alergias",
                        "/api/pacientes/documento/*/antecedentes-personales",
                        "/api/pacientes/documento/*/antecedentes-familiares",
                        "/api/pacientes/documento/*/bitacora").hasAnyRole(ADMIN, MEDICO)
                .requestMatchers(HttpMethod.DELETE, "/api/pacientes/**").hasRole(ADMIN)
                .requestMatchers("/api/pacientes/**", "/api/atenciones/**").hasAnyRole(ADMIN, MEDICO, RECEPCION)

                // Seguros y contactos: MEDICO solo lectura
                .requestMatchers(HttpMethod.GET, "/api/seguros/**", "/api/contactos-emergencia/**")
                        .hasAnyRole(ADMIN, MEDICO, RECEPCION)
                .requestMatchers("/api/seguros/**", "/api/contactos-emergencia/**").hasAnyRole(ADMIN, RECEPCION)

                // Todo lo demás: solo ADMINISTRADOR (denegar por defecto al resto)
                .anyRequest().hasRole(ADMIN))

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(new RedireccionPorRolSuccessHandler(requestCache, evaluador))
                .failureHandler(fallo)
                .permitAll())

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID"))

            // Cookie XSRF-TOKEN legible por JS (withHttpOnlyFalse) + header X-XSRF-TOKEN desde static/js/api.js
            .csrf(csrf -> csrf.spa())
            .addFilterAfter(cargarTokenCsrf, CsrfFilter.class)

            .requestCache(cache -> cache.requestCache(requestCache))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(errores)
                .accessDeniedHandler(errores))

            .addFilterBefore(new UsuarioVigenteFilter(usuarioDetailsService), AuthorizationFilter.class);

        return http.build();
    }
}
