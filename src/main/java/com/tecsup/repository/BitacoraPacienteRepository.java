package com.tecsup.repository;

import com.tecsup.model.BitacoraPaciente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BitacoraPacienteRepository extends JpaRepository<BitacoraPaciente, Integer> {
    @EntityGraph(attributePaths = {"paciente", "paciente.tipoDocumento", "paciente.ubigeo", "usuario", "usuario.rol"})
    List<BitacoraPaciente> findByPacienteIdPacienteOrderByFechaHoraDesc(Integer idPaciente);

    @EntityGraph(attributePaths = {"paciente", "paciente.tipoDocumento", "paciente.ubigeo", "usuario", "usuario.rol"})
    List<BitacoraPaciente> findByPaciente_NumeroDocumentoOrderByFechaHoraDesc(String numeroDocumento);
}