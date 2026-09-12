package com.tecsup.service;

import com.tecsup.model.Paciente;
import com.tecsup.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;

    public List<Paciente> listarPacientes() {
        return pacienteRepository.findAll();
    }


    public Paciente registrarPaciente(Paciente paciente) {

        long total = pacienteRepository.count();
        String codigoAutogenerado = String.format("PAC-%06d", total + 1);
        paciente.setCodigo(codigoAutogenerado);

        return pacienteRepository.save(paciente);
    }
}