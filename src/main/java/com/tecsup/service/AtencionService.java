package com.tecsup.service;

import com.tecsup.model.Atencion;
import com.tecsup.model.Paciente;
import com.tecsup.repository.AtencionRepository;
import com.tecsup.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AtencionService {

    @Autowired
    private AtencionRepository repository;
    @Autowired
    private PacienteRepository pacienteRepository;

    public List<Atencion> listarPorPaciente(String numeroDocumento) {
        return repository.findByPaciente_NumeroDocumentoOrderByFechaAtencionDesc(numeroDocumento);
    }

    public Atencion registrar(Atencion atencion) {
        if (atencion.getPaciente() == null || atencion.getPaciente().getNumeroDocumento() == null)
            throw new RuntimeException("Debes indicar el número de documento del paciente.");
        Paciente paciente = pacienteRepository.findByNumeroDocumento(atencion.getPaciente().getNumeroDocumento())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado (documento " + atencion.getPaciente().getNumeroDocumento() + ")."));
        atencion.setPaciente(paciente);
        return repository.save(atencion);
    }

    public List<Atencion> listarTodas() {
        return repository.findAll();
    }

    public void eliminar(Integer id) {
        repository.deleteById(id);
    }
}