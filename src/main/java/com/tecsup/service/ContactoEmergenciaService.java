package com.tecsup.service;

import com.tecsup.model.ContactoEmergencia;
import com.tecsup.model.Paciente;
import com.tecsup.repository.ContactoEmergenciaRepository;
import com.tecsup.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ContactoEmergenciaService {

    @Autowired private ContactoEmergenciaRepository repository;
    @Autowired private PacienteRepository pacienteRepository;

    public ContactoEmergencia registrar(ContactoEmergencia c) {
        if (c.getPaciente() == null || c.getPaciente().getNumeroDocumento() == null)
            throw new RuntimeException("Debes indicar el número de documento del paciente.");
        Paciente paciente = pacienteRepository.findByNumeroDocumento(c.getPaciente().getNumeroDocumento())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado (documento " + c.getPaciente().getNumeroDocumento() + ")."));
        c.setPaciente(paciente);
        return repository.save(c);
    }

    public List<ContactoEmergencia> listarPorPaciente(String numeroDocumento) {
        return repository.findByPaciente_NumeroDocumento(numeroDocumento);
    }

    public ContactoEmergencia actualizar(Integer id, ContactoEmergencia datos) {
        ContactoEmergencia e = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contacto no encontrado."));
        e.setNombreCompleto(datos.getNombreCompleto());
        e.setParentesco(datos.getParentesco());
        e.setTelefono(datos.getTelefono());
        e.setDireccion(datos.getDireccion());
        e.setCorreoElectronico(datos.getCorreoElectronico());
        e.setEsPrincipal(datos.isEsPrincipal());
        return repository.save(e);
    }

    public void eliminar(Integer id) {
        repository.deleteById(id);
    }

    public Optional<ContactoEmergencia> obtenerPorId(Integer id) {
        return repository.findById(id);
    }
}