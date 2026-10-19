package com.tecsup.repository;

import com.tecsup.model.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

// Búsqueda filtrada y paginada con JpaSpecificationExecutor (ver AuditoriaService.consultar)
@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long>, JpaSpecificationExecutor<Auditoria> {

    @Query("select distinct a.entidad from Auditoria a order by a.entidad")
    List<String> listarEntidades();
}
