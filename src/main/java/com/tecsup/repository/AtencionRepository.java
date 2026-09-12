package com.tecsup.repository;

import com.tecsup.model.Atencion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AtencionRepository extends JpaRepository<Atencion, Integer> {
    List<Atencion> findByPacienteIdPacienteOrderByFechaAtencionDesc(Integer idPaciente);
}