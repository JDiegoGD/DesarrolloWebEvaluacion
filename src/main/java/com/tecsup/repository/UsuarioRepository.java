package com.tecsup.repository;

import com.tecsup.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // rol es LAZY: se carga en el mismo SELECT para poder serializarlo fuera de la transacción
    @Override
    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> findById(Integer id);

    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    boolean existsByNombreUsuario(String nombreUsuario);
    boolean existsByCorreo(String correo);
    boolean existsByCorreoAndIdUsuarioNot(String correo, Integer idUsuario);
    boolean existsByRol_Nombre(String nombreRol);

    // Filtros opcionales: rolId, nombre de rol (filtro antiguo ?rol=), activo y texto libre
    @Query("select u from Usuario u join fetch u.rol r "
            + "where (:rolId is null or r.idRol = :rolId) "
            + "and (:rol is null or upper(r.nombre) = upper(:rol)) "
            + "and (:activo is null or u.activo = :activo) "
            + "and (:q is null or lower(u.nombreUsuario) like lower(concat('%', :q, '%')) "
            + "     or lower(u.nombres) like lower(concat('%', :q, '%'))) "
            + "order by u.nombreUsuario")
    List<Usuario> buscar(@Param("rolId") Integer rolId, @Param("rol") String rol,
                         @Param("activo") Boolean activo, @Param("q") String q);

    // Administradores activos con el rol ADMINISTRADOR también activo
    @Query("select count(u) from Usuario u where u.rol.nombre = 'ADMINISTRADOR' "
            + "and u.activo = true and u.rol.activo = true")
    long contarAdministradoresActivos();
}
