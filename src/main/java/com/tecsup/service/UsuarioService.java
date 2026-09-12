package com.tecsup.service;

import com.tecsup.model.Usuario;
import com.tecsup.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired private UsuarioRepository repository;

    public Usuario registrar(Usuario u) { return repository.save(u); }

    public List<Usuario> listarTodos() { return repository.findAll(); }

    public List<Usuario> listarPorRol(String rol) {
        return repository.findByRol(rol);
    }

    public Optional<Usuario> obtenerPorId(Integer id) {
        return repository.findById(id);
    }

    public void eliminar(Integer id) { repository.deleteById(id); }
}