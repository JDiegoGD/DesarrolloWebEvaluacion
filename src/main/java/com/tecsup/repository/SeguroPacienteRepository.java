package com.tecsup.repository;

import com.tecsup.model.SeguroPaciente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeguroPacienteRepository extends JpaRepository<SeguroPaciente, Integer> {
    @Override
    @EntityGraph(attributePaths = {"paciente", "paciente.tipoDocumento", "paciente.ubigeo"})
    Optional<SeguroPaciente> findById(Integer id);

    @EntityGraph(attributePaths = {"paciente", "paciente.tipoDocumento", "paciente.ubigeo"})
    List<SeguroPaciente> findByPacienteIdPaciente(Integer idPaciente);

    @EntityGraph(attributePaths = {"paciente", "paciente.tipoDocumento", "paciente.ubigeo"})
    List<SeguroPaciente> findByPaciente_NumeroDocumento(String numeroDocumento);
}