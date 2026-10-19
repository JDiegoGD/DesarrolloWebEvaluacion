package com.tecsup.repository;

import com.tecsup.model.ContactoEmergencia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContactoEmergenciaRepository extends JpaRepository<ContactoEmergencia, Integer> {
    @Override
    @EntityGraph(attributePaths = {"paciente", "paciente.tipoDocumento", "paciente.ubigeo"})
    Optional<ContactoEmergencia> findById(Integer id);

    @EntityGraph(attributePaths = {"paciente", "paciente.tipoDocumento", "paciente.ubigeo"})
    List<ContactoEmergencia> findByPacienteIdPaciente(Integer idPaciente);

    @EntityGraph(attributePaths = {"paciente", "paciente.tipoDocumento", "paciente.ubigeo"})
    List<ContactoEmergencia> findByPaciente_NumeroDocumento(String numeroDocumento);
}