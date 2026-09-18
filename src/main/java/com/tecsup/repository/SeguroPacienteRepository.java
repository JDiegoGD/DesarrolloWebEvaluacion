package com.tecsup.repository;

import com.tecsup.model.SeguroPaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeguroPacienteRepository extends JpaRepository<SeguroPaciente, Integer> {
    List<SeguroPaciente> findByPacienteIdPaciente(Integer idPaciente);

    List<SeguroPaciente> findByPaciente_NumeroDocumento(String numeroDocumento);
}