package com.tecsup.repository;

import com.tecsup.model.Paciente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {

    boolean existsByNumeroDocumento(String numeroDocumento);
    boolean existsByCorreoElectronico(String correoElectronico);
    boolean existsByTelefono(String telefono);
    // tipoDocumento y ubigeo son LAZY: se cargan en el mismo SELECT (sin N+1 ni LazyInitializationException)
    @Override
    @EntityGraph(attributePaths = {"tipoDocumento", "ubigeo"})
    List<Paciente> findAll();

    @EntityGraph(attributePaths = {"tipoDocumento", "ubigeo"})
    Optional<Paciente> findByCodigoPaciente(String codigoPaciente);
    @EntityGraph(attributePaths = {"tipoDocumento", "ubigeo"})
    Optional<Paciente> findByNumeroDocumento(String numeroDocumento);
    @EntityGraph(attributePaths = {"tipoDocumento", "ubigeo"})
    List<Paciente> findByNombresContainingIgnoreCase(String nombres);
    @EntityGraph(attributePaths = {"tipoDocumento", "ubigeo"})
    List<Paciente> findByApellidoPaternoContainingIgnoreCase(String apellidoPaterno);
    @EntityGraph(attributePaths = {"tipoDocumento", "ubigeo"})
    List<Paciente> findByApellidoMaternoContainingIgnoreCase(String apellidoMaterno);
}