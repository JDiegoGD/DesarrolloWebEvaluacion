package com.tecsup.service;

import com.tecsup.dto.UsuarioRequestDTO;
import com.tecsup.dto.UsuarioResponseDTO;
import com.tecsup.exception.ConflictoException;
import com.tecsup.exception.RecursoNoEncontradoException;
import com.tecsup.exception.SolicitudInvalidaException;
import com.tecsup.model.Rol;
import com.tecsup.model.Usuario;
import com.tecsup.repository.RolRepository;
import com.tecsup.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired private UsuarioRepository repository;
    @Autowired private RolRepository rolRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponseDTO crear(UsuarioRequestDTO dto) {
        String nombreUsuario = dto.nombreUsuario().trim();
        if (repository.existsByNombreUsuario(nombreUsuario))
            throw new ConflictoException("El nombre de usuario '" + nombreUsuario + "' ya existe.");
        String correo = normalizarCorreo(dto.correo());
        if (correo != null && repository.existsByCorreo(correo))
            throw new ConflictoException("El correo '" + correo + "' ya está registrado por otro usuario.");

        Usuario u = new Usuario();
        u.setNombreUsuario(nombreUsuario);
        u.setNombres(dto.nombres().trim());
        u.setCorreo(correo);
        u.setPassword(passwordEncoder.encode(dto.password()));
        u.setRol(obtenerRolActivo(dto.rolId()));
        u.setActivo(true);
        return UsuarioResponseDTO.de(repository.save(u));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar(Integer rolId, String rol, Boolean activo, String q) {
        String texto = (q == null || q.isBlank()) ? null : q.trim();
        String nombreRol = (rol == null || rol.isBlank()) ? null : rol.trim();
        return repository.buscar(rolId, nombreRol, activo, texto).stream()
                .map(UsuarioResponseDTO::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(Integer id) {
        return UsuarioResponseDTO.de(obtenerEntidad(id));
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioResponseDTO> obtenerPorNombreUsuario(String nombreUsuario) {
        return repository.findByNombreUsuario(nombreUsuario).map(UsuarioResponseDTO::de);
    }

    // Edita nombres, correo, rol y, si viene, el password (el nombre de usuario no se cambia)
    @Transactional
    public UsuarioResponseDTO actualizar(Integer id, UsuarioRequestDTO dto) {
        Usuario u = obtenerEntidad(id);
        if (dto.nombreUsuario() != null && !dto.nombreUsuario().trim().equalsIgnoreCase(u.getNombreUsuario()))
            throw new SolicitudInvalidaException("El nombre de usuario no se puede modificar.");
        String correo = normalizarCorreo(dto.correo());
        if (correo != null && repository.existsByCorreoAndIdUsuarioNot(correo, id))
            throw new ConflictoException("El correo '" + correo + "' ya está registrado por otro usuario.");

        cambiarRolValidado(u, dto.rolId());
        u.setNombres(dto.nombres().trim());
        u.setCorreo(correo);
        if (dto.password() != null && !dto.password().isBlank())
            u.setPassword(passwordEncoder.encode(dto.password()));
        return UsuarioResponseDTO.de(u);
    }

    @Transactional
    public UsuarioResponseDTO cambiarEstado(Integer id, boolean activo) {
        Usuario u = obtenerEntidad(id);
        if (activo && !u.getRol().isActivo())
            throw new SolicitudInvalidaException("No se puede activar al usuario: su rol " + u.getRol().getNombre()
                    + " está inactivo. Asígnale un rol activo primero.");
        if (!activo) validarNoEsUltimoAdministrador(u, "desactivar");
        u.setActivo(activo);
        return UsuarioResponseDTO.de(u);
    }

    @Transactional
    public UsuarioResponseDTO cambiarRol(Integer id, Integer rolId) {
        Usuario u = obtenerEntidad(id);
        cambiarRolValidado(u, rolId);
        return UsuarioResponseDTO.de(u);
    }

    // DELETE físico existente: 409 por FK si tiene atenciones o bitácora (se prefiere desactivar)
    @Transactional
    public void eliminar(String nombreUsuario) {
        Usuario u = repository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
        validarNoEsUltimoAdministrador(u, "eliminar");
        repository.delete(u);
        repository.flush();
    }

    private Usuario obtenerEntidad(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado (id " + id + ")."));
    }

    private Rol obtenerRolActivo(Integer rolId) {
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado (id " + rolId + ")."));
        if (!rol.isActivo())
            throw new SolicitudInvalidaException("El rol " + rol.getNombre() + " está inactivo y no se puede asignar.");
        return rol;
    }

    private void cambiarRolValidado(Usuario u, Integer rolId) {
        if (u.getRol().getIdRol().equals(rolId)) return;
        Rol nuevo = obtenerRolActivo(rolId);
        validarNoEsUltimoAdministrador(u, "cambiar de rol");
        u.setRol(nuevo);
    }

    private void validarNoEsUltimoAdministrador(Usuario u, String accion) {
        boolean esAdminActivo = u.isActivo() && u.getRol().isActivo()
                && Rol.ADMINISTRADOR.equals(u.getRol().getNombre());
        if (esAdminActivo && repository.contarAdministradoresActivos() <= 1)
            throw new ConflictoException("No se puede " + accion + " a '" + u.getNombreUsuario()
                    + "': es el último ADMINISTRADOR activo del sistema.");
    }

    private String normalizarCorreo(String correo) {
        return (correo == null || correo.isBlank()) ? null : correo.trim().toLowerCase();
    }
}
