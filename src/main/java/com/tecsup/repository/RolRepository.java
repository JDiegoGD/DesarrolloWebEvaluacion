package com.tecsup.repository;

import com.tecsup.dto.RolResponseDTO;
import com.tecsup.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    // Los nombres se guardan en mayúsculas, así la comparación no depende de mayúsculas/minúsculas
    Optional<Rol> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdRolNot(String nombre, Integer idRol);

    @Query("select count(u) from Usuario u where u.rol.idRol = :idRol and u.activo = true")
    long contarUsuariosActivos(@Param("idRol") Integer idRol);

    // Roles con su cantidad de usuarios asignados en una sola consulta (sin N+1)
    @Query("select new com.tecsup.dto.RolResponseDTO(r.idRol, r.nombre, r.descripcion, r.activo, count(u)) "
            + "from Rol r left join r.usuarios u "
            + "where (:activo is null or r.activo = :activo) "
            + "group by r.idRol, r.nombre, r.descripcion, r.activo order by r.nombre")
    List<RolResponseDTO> listarConCantidadUsuarios(@Param("activo") Boolean activo);

    @Query("select new com.tecsup.dto.RolResponseDTO(r.idRol, r.nombre, r.descripcion, r.activo, count(u)) "
            + "from Rol r left join r.usuarios u where r.idRol = :idRol "
            + "group by r.idRol, r.nombre, r.descripcion, r.activo")
    Optional<RolResponseDTO> obtenerConCantidadUsuarios(@Param("idRol") Integer idRol);
}
