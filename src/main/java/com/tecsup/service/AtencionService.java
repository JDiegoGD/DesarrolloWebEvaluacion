package com.tecsup.service;

import com.tecsup.auditoria.Auditable;
import com.tecsup.enums.OperacionAuditoria;
import com.tecsup.model.Atencion;
import com.tecsup.model.Paciente;
import com.tecsup.model.Usuario;
import com.tecsup.repository.AtencionRepository;
import com.tecsup.exception.RecursoNoEncontradoException;
import com.tecsup.exception.SolicitudInvalidaException;
import com.tecsup.model.Rol;
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

    @Auditable(entidad = "Atencion", operacion = OperacionAuditoria.REGISTRO,
            detalle = "'Se registró una atención del paciente ' + #resultado.paciente.numeroDocumento + (#resultado.medico != null ? ' con el médico ' + #resultado.medico.nombreUsuario : ' sin médico')")
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
            if (!Rol.MEDICO.equals(medico.getRol().getNombre()))
                throw new SolicitudInvalidaException("El usuario '" + medico.getNombreUsuario()
                        + "' no tiene el rol MEDICO (tiene " + medico.getRol().getNombre() + ").");
            if (!medico.isActivo())
                throw new SolicitudInvalidaException("El médico '" + medico.getNombreUsuario() + "' está inactivo.");
            atencion.setMedico(medico);
        }
        return repository.save(atencion);
    }

    @Transactional(readOnly = true)
    public List<Atencion> listarTodas() {
        return repository.findAll();
    }

    @Auditable(entidad = "Atencion", operacion = OperacionAuditoria.ELIMINACION,
            detalle = "'Se eliminó la atención ' + #id")
    public void eliminar(Integer id) {
        repository.deleteById(id);
    }
}