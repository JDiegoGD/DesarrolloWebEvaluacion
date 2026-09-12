package com.tecsup.repository;

import com.tecsup.model.BitacoraPaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BitacoraPacienteRepository extends JpaRepository<BitacoraPaciente, Integer> {
    List<BitacoraPaciente> findByPacienteIdPacienteOrderByFechaHoraDesc(Integer idPaciente);
}