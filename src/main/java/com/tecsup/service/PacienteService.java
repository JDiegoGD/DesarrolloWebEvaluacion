package com.tecsup.service;

import com.tecsup.model.*;
import com.tecsup.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PacienteService {

    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private BitacoraPacienteRepository bitacoraRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    // RF-PAC-01: registrar paciente
    // RF-PAC-02: validar documento no duplicado
    public Paciente registrarPaciente(Paciente paciente) {
        if (pacienteRepository.existsByNumeroDocumento(paciente.getNumeroDocumento()))
            throw new RuntimeException("El número de documento ya está registrado.");
        paciente.setCodigoPaciente(generarCodigo()); // RF-PAC-03 (INT-2)
        paciente.setEstado("Activo");
        return pacienteRepository.save(paciente);
    }

    // RF-PAC-03: generarCodigo() — implementado por INT-2
    String generarCodigo() {
        String codigo;
        do {
            codigo = "PAC-" + UUID.randomUUID().toString().substring(0,8).toUpperCase();
        } while (pacienteRepository.findByCodigoPaciente(codigo).isPresent());
        return codigo;
    }

    // RF-PAC-05: búsquedas — implementadas por INT-3
    public Optional<Paciente> buscarPorDocumento(String num) {
        return pacienteRepository.findByNumeroDocumento(num);
    }
    public Optional<Paciente> buscarPorCodigo(String cod) {
        return pacienteRepository.findByCodigoPaciente(cod);
    }
    public List<Paciente> buscarPorNombres(String n) {
        return pacienteRepository.findByNombresContainingIgnoreCase(n);
    }
    public List<Paciente> buscarPorApellidoPaterno(String a) {
        return pacienteRepository.findByApellidoPaternoContainingIgnoreCase(a);
    }
    public List<Paciente> buscarPorApellidoMaterno(String a) {
        return pacienteRepository.findByApellidoMaternoContainingIgnoreCase(a);
    }
    public Optional<Paciente> obtenerPacienteCompleto(Integer id) {
        return pacienteRepository.findById(id);
    }
    public List<Paciente> listarTodos() { return pacienteRepository.findAll(); }

    // RF-PAC-08: actualizar — implementado por INT-4
    public Paciente actualizarPaciente(Integer id, Paciente datos, Integer idUsuario) {
        Paciente e = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado."));
        Usuario u = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        registrarBitacora(e, u, "nombres", e.getNombres(), datos.getNombres());
        registrarBitacora(e, u, "telefono", e.getTelefono(), datos.getTelefono());
        registrarBitacora(e, u, "correoElectronico", e.getCorreoElectronico(), datos.getCorreoElectronico());
        registrarBitacora(e, u, "estado", e.getEstado(), datos.getEstado());
        e.setNombres(datos.getNombres());
        e.setApellidoPaterno(datos.getApellidoPaterno());
        e.setApellidoMaterno(datos.getApellidoMaterno());
        e.setTelefono(datos.getTelefono());
        e.setCorreoElectronico(datos.getCorreoElectronico());
        e.setDireccion(datos.getDireccion());
        e.setOcupacion(datos.getOcupacion());
        e.setEstadoCivil(datos.getEstadoCivil());
        e.setFotografiaUrl(datos.getFotografiaUrl());
        e.setEstado(datos.getEstado());
        return pacienteRepository.save(e);
    }

    // RF-PAC-13: no eliminar físicamente si tiene atenciones
    public void eliminarPaciente(Integer id) {
        Paciente p = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado."));
        if (p.getAtenciones() != null && !p.getAtenciones().isEmpty()) {
            p.setEstado("Inactivo"); // solo desactiva
            pacienteRepository.save(p);
        } else {
            pacienteRepository.delete(p);
        }
    }

    // RF-PAC-12: bitácora — usado por INT-4 y INT-6
    public void registrarBitacora(Paciente p, Usuario u,
                                  String campo, String anterior, String nuevo) {
        if (anterior != null && anterior.equals(nuevo)) return;
        BitacoraPaciente log = new BitacoraPaciente();
        log.setPaciente(p); log.setUsuario(u);
        log.setFechaHora(LocalDateTime.now());
        log.setAccion("edición");
        log.setCampoModificado(campo);
        log.setValorAnterior(anterior);
        log.setValorNuevo(nuevo);
        bitacoraRepository.save(log);
    }

    public List<BitacoraPaciente> obtenerBitacora(Integer id) {
        return bitacoraRepository.findByPacienteIdPacienteOrderByFechaHoraDesc(id);
    }
}