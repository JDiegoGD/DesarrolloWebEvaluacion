package com.tecsup.service;

import com.tecsup.model.Atencion;
import com.tecsup.repository.AtencionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AtencionService {

    @Autowired
    private AtencionRepository repository;

    public List<Atencion> listarPorPaciente(Integer idPaciente) {
        return repository.findByPacienteIdPacienteOrderByFechaAtencionDesc(idPaciente);
    }

    public Atencion registrar(Atencion atencion) {
        return repository.save(atencion);
    }

    public List<Atencion> listarTodas() {
        return repository.findAll();
    }

    public void eliminar(Integer id) {
        repository.deleteById(id);
    }
}