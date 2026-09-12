package com.tecsup.repository;

import com.tecsup.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {

    // RF-PAC-02: validar duplicado
    boolean existsByNumeroDocumento(String numeroDocumento);

    Optional<Paciente> findByCodigoPaciente(String codigoPaciente);
    Optional<Paciente> findByNumeroDocumento(String numeroDocumento);
    List<Paciente> findByNombresContainingIgnoreCase(String nombres);
    List<Paciente> findByApellidoPaternoContainingIgnoreCase(String apellidoPaterno);
    List<Paciente> findByApellidoMaternoContainingIgnoreCase(String apellidoMaterno);
}