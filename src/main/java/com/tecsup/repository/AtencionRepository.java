package com.tecsup.repository;

import com.tecsup.model.Atencion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AtencionRepository extends JpaRepository<Atencion, Integer> {

    @Override
    @EntityGraph(attributePaths = {"paciente", "paciente.tipoDocumento", "paciente.ubigeo", "medico"})
    List<Atencion> findAll();

    @EntityGraph(attributePaths = {"paciente", "paciente.tipoDocumento", "paciente.ubigeo", "medico"})
    List<Atencion> findByPacienteIdPacienteOrderByFechaAtencionDesc(Integer idPaciente);

    @EntityGraph(attributePaths = {"paciente", "paciente.tipoDocumento", "paciente.ubigeo", "medico"})
    List<Atencion> findByPaciente_NumeroDocumentoOrderByFechaAtencionDesc(String numeroDocumento);

    long countByPacienteIdPaciente(Integer idPaciente);
}