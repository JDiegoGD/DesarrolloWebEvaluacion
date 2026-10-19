package com.tecsup.service;

import com.tecsup.auditoria.Auditable;
import com.tecsup.enums.OperacionAuditoria;
import com.tecsup.dto.RolRequestDTO;
import com.tecsup.dto.RolResponseDTO;
import com.tecsup.exception.ConflictoException;
import com.tecsup.exception.RecursoNoEncontradoException;
import com.tecsup.model.Rol;
import com.tecsup.repository.RolRepository;
import com.tecsup.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;

@Service
public class RolService {

    @Autowired private RolRepository repository;
    @Autowired private UsuarioRepository usuarioRepository;

    @Auditable(entidad = "Rol", operacion = OperacionAuditoria.REGISTRO,
            detalle = "'Se registró el rol ' + #resultado.nombre")
    @Transactional
    public RolResponseDTO crear(RolRequestDTO dto) {
        String nombre = normalizarNombre(dto.nombre());
        if (repository.existsByNombre(nombre))
            throw new ConflictoException("Ya existe un rol con el nombre '" + nombre + "'.");
        Rol rol = new Rol();
        rol.setNombre(nombre);
        rol.setDescripcion(dto.descripcion());
        rol.setActivo(true);
        repository.save(rol);
        return obtener(rol.getIdRol());
    }

    @Transactional(readOnly = true)
    public List<RolResponseDTO> listar(Boolean activo) {
        return repository.listarConCantidadUsuarios(activo);
    }

    @Transactional(readOnly = true)
    public RolResponseDTO obtener(Integer id) {
        return repository.obtenerConCantidadUsuarios(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado (id " + id + ")."));
    }

    @Auditable(entidad = "Rol", operacion = OperacionAuditoria.MODIFICACION,
            detalle = "'Se modificó el rol ' + #resultado.nombre")
    @Transactional
    public RolResponseDTO actualizar(Integer id, RolRequestDTO dto) {
        Rol rol = obtenerEntidad(id);
        String nombre = normalizarNombre(dto.nombre());
        if (repository.existsByNombreAndIdRolNot(nombre, id))
            throw new ConflictoException("Ya existe otro rol con el nombre '" + nombre + "'.");
        // El nombre ADMINISTRADOR lo usan las reglas del último administrador y la semilla
        if (Rol.ADMINISTRADOR.equals(rol.getNombre()) && !Rol.ADMINISTRADOR.equals(nombre))
            throw new ConflictoException("El rol ADMINISTRADOR no se puede renombrar.");
        rol.setNombre(nombre);
        rol.setDescripcion(dto.descripcion());
        repository.flush();
        return obtener(id);
    }

    @Auditable(entidad = "Rol", estado = "#activo",
            detalle = "'Se ' + (#activo ? 'activó' : 'desactivó') + ' el rol ' + #resultado.nombre")
    @Transactional
    public RolResponseDTO cambiarEstado(Integer id, boolean activo) {
        Rol rol = obtenerEntidad(id);
        if (!activo && rol.isActivo()) {
            long activos = repository.contarUsuariosActivos(id);
            if (Rol.ADMINISTRADOR.equals(rol.getNombre()) && activos > 0
                    && usuarioRepository.contarAdministradoresActivos() == activos)
                throw new ConflictoException("No se puede desactivar el rol ADMINISTRADOR: es el único rol con "
                        + "administradores activos (" + activos + ").");
            if (activos > 0)
                throw new ConflictoException("No se puede desactivar el rol " + rol.getNombre() + ": tiene "
                        + activos + " usuario(s) activo(s) asignado(s). Reasígnalos o desactívalos primero.");
        }
        rol.setActivo(activo);
        repository.flush();
        return obtener(id);
    }

    private Rol obtenerEntidad(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado (id " + id + ")."));
    }

    // Nombres en mayúsculas: la unicidad no depende de mayúsculas/minúsculas
    private String normalizarNombre(String nombre) {
        return nombre.trim().toUpperCase(Locale.ROOT);
    }
}
