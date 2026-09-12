package com.tecsup.service;

import com.tecsup.model.ContactoEmergencia;
import com.tecsup.repository.ContactoEmergenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ContactoEmergenciaService {

    @Autowired private ContactoEmergenciaRepository repository;

    public ContactoEmergencia registrar(ContactoEmergencia c) {
        return repository.save(c);
    }

    public List<ContactoEmergencia> listarPorPaciente(Integer id) {
        return repository.findByPacienteIdPaciente(id);
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