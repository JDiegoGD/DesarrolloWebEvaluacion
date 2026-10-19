package com.tecsup.config;

import com.tecsup.model.Rol;
import com.tecsup.model.Usuario;
import com.tecsup.repository.RolRepository;
import com.tecsup.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

// Semilla idempotente: solo crea lo que falta, se puede ejecutar en cada arranque
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final Map<String, String> ROLES_BASE = Map.of(
            Rol.ADMINISTRADOR, "Administra usuarios, roles y catálogos del sistema",
            Rol.MEDICO, "Registra y consulta atenciones de pacientes",
            Rol.RECEPCIONISTA, "Registra pacientes, seguros y contactos");

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RolRepository rolRepository, UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ROLES_BASE.forEach((nombre, descripcion) -> {
            if (!rolRepository.existsByNombre(nombre)) {
                Rol rol = new Rol();
                rol.setNombre(nombre);
                rol.setDescripcion(descripcion);
                rolRepository.save(rol);
                log.info("DataInitializer: rol {} creado.", nombre);
            }
        });

        if (usuarioRepository.existsByRol_Nombre(Rol.ADMINISTRADOR)) {
            log.info("DataInitializer: ya existe un usuario ADMINISTRADOR, no se crea el usuario inicial.");
            return;
        }
        if (usuarioRepository.existsByNombreUsuario("admin")) {
            log.warn("DataInitializer: no hay ningún ADMINISTRADOR, pero el nombre 'admin' ya lo usa otro usuario. "
                    + "Asigna el rol ADMINISTRADOR manualmente.");
            return;
        }
        Usuario admin = new Usuario();
        admin.setNombreUsuario("admin");
        admin.setNombres("Administrador del Sistema");
        admin.setPassword(passwordEncoder.encode("Admin123*"));
        admin.setRol(rolRepository.findByNombre(Rol.ADMINISTRADOR).orElseThrow());
        admin.setActivo(true);
        usuarioRepository.save(admin);
        log.info("DataInitializer: usuario administrador inicial 'admin' creado (cambia su contraseña).");
    }
}
