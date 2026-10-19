package com.tecsup.service;

import com.tecsup.model.Atencion;
import com.tecsup.model.Paciente;
import com.tecsup.model.Usuario;
import com.tecsup.repository.AtencionRepository;
import com.tecsup.exception.RecursoNoEncontradoException;
import com.tecsup.repository.PacienteRepository;
import com.tecsup.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AtencionService {

    @Autowired
    private AtencionRepository repository;
    @Autowired
    private PacienteRepository pacienteRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<Atencion> listarPorPaciente(String numeroDocumento) {
        return repository.findByPaciente_NumeroDocumentoOrderByFechaAtencionDesc(numeroDocumento);
    }

    @Transactional
    public Atencion registrar(Atencion atencion) {
        if (atencion.getPaciente() == null || atencion.getPaciente().getNumeroDocumento() == null)
            throw new RuntimeException("Debes indicar el número de documento del paciente.");
        Paciente paciente = pacienteRepository.findByNumeroDocumento(atencion.getPaciente().getNumeroDocumento())
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado (documento " + atencion.getPaciente().getNumeroDocumento() + ")."));
        atencion.setPaciente(paciente);
        // El médico es opcional: si no se envía idMedico la atención queda sin médico
        if (atencion.getIdMedico() != null) {
            Usuario medico = usuarioRepository.findById(atencion.getIdMedico())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Médico no encontrado (id " + atencion.getIdMedico() + ")."));
            atencion.setMedico(medico);
        }
        return repository.save(atencion);
    }

    @Transactional(readOnly = true)
    public List<Atencion> listarTodas() {
        return repository.findAll();
    }

    public void eliminar(Integer id) {
        repository.deleteById(id);
    }
}