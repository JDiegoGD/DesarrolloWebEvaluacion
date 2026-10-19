package com.tecsup.service;

import com.tecsup.model.Paciente;
import com.tecsup.model.SeguroPaciente;
import com.tecsup.exception.RecursoNoEncontradoException;
import com.tecsup.repository.PacienteRepository;
import com.tecsup.repository.SeguroPacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class SeguroPacienteService {

    @Autowired private SeguroPacienteRepository repository;
    @Autowired private PacienteRepository pacienteRepository;

    @Transactional
    public SeguroPaciente registrar(SeguroPaciente s) {
        if (s.getPaciente() == null || s.getPaciente().getNumeroDocumento() == null)
            throw new RuntimeException("Debes indicar el número de documento del paciente.");
        Paciente paciente = pacienteRepository.findByNumeroDocumento(s.getPaciente().getNumeroDocumento())
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado (documento " + s.getPaciente().getNumeroDocumento() + ")."));
        paciente.addSeguro(s);
        return repository.save(s);
    }

    @Transactional(readOnly = true)
    public List<SeguroPaciente> listarPorPaciente(String numeroDocumento) {
        return repository.findByPaciente_NumeroDocumento(numeroDocumento);
    }

    @Transactional
    public SeguroPaciente actualizar(Integer id, SeguroPaciente datos) {
        SeguroPaciente e = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Seguro no encontrado."));
        e.setTipoSeguro(datos.getTipoSeguro());
        e.setEmpresaAseguradora(datos.getEmpresaAseguradora());
        e.setNumeroPoliza(datos.getNumeroPoliza());
        e.setNumeroAfiliacion(datos.getNumeroAfiliacion());
        e.setFechaInicio(datos.getFechaInicio());
        e.setFechaVencimiento(datos.getFechaVencimiento());
        e.setEstadoCobertura(datos.getEstadoCobertura());
        return repository.save(e);
    }

    // Se quita de la lista del paciente y orphanRemoval lo elimina
    @Transactional
    public void eliminar(Integer id) {
        SeguroPaciente e = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Seguro no encontrado (id " + id + ")."));
        e.getPaciente().removeSeguro(e);
    }

    public Optional<SeguroPaciente> obtenerPorId(Integer id) {
        return repository.findById(id);
    }
}