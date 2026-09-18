package com.tecsup.service;

import com.tecsup.model.Paciente;
import com.tecsup.model.BitacoraPaciente;
import com.tecsup.model.TipoDocumento;
import com.tecsup.model.Usuario;
import com.tecsup.repository.PacienteRepository;
import com.tecsup.repository.BitacoraPacienteRepository;
import com.tecsup.repository.TipoDocumentoRepository;
import com.tecsup.repository.UsuarioRepository;
import com.tecsup.repository.AtencionRepository;
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
    @Autowired private AtencionRepository atencionRepository;
    @Autowired private TipoDocumentoRepository tipoDocumentoRepository;

    // RF-PAC-01 + RF-PAC-02
    public Paciente registrarPaciente(Paciente paciente) {
        if (pacienteRepository.existsByNumeroDocumento(paciente.getNumeroDocumento()))
            throw new RuntimeException("El número de documento ya está registrado.");
        if (paciente.getTipoDocumento() == null || paciente.getTipoDocumento().getCodigo() == null)
            throw new RuntimeException("Debes indicar el código del tipo de documento.");
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findByCodigo(paciente.getTipoDocumento().getCodigo())
                .orElseThrow(() -> new RuntimeException("Tipo de documento no reconocido: " + paciente.getTipoDocumento().getCodigo()));
        paciente.setTipoDocumento(tipoDocumento);
        paciente.setCodigoPaciente(generarCodigo());
        paciente.setEstado("Activo");
        return pacienteRepository.save(paciente);
    }

    private Paciente obtenerEntidadPorDocumento(String numeroDocumento) {
        return pacienteRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado (documento " + numeroDocumento + ")."));
    }

    // RF-PAC-03
    private String generarCodigo() {
        String codigo;
        do {
            codigo = "PAC-" + UUID.randomUUID()
                    .toString()
                    .substring(0, 8)
                    .toUpperCase();
        } while (pacienteRepository.findByCodigoPaciente(codigo).isPresent());
        return codigo;
    }

    // RF-PAC-05
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

    // RF-PAC-06
    public List<Paciente> listarTodos() {
        return pacienteRepository.findAll();
    }

    // RF-PAC-08
    public Paciente actualizarPaciente(String numeroDocumento, Paciente datos, String nombreUsuario) {
        Paciente e = obtenerEntidadPorDocumento(numeroDocumento);
        Usuario u = usuarioRepository.findByNombreUsuario(nombreUsuario)
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

    // RF-PAC-13: eliminación lógica sin lazy loading
    public void eliminarPaciente(String numeroDocumento) {
        Paciente p = obtenerEntidadPorDocumento(numeroDocumento);
        long cantidadAtenciones = atencionRepository.countByPacienteIdPaciente(p.getIdPaciente());
        if (cantidadAtenciones > 0) {
            p.setEstado("Inactivo");
            pacienteRepository.save(p);
        } else {
            pacienteRepository.delete(p);
        }
    }

    // RF-PAC-12
    public void registrarBitacora(Paciente paciente, Usuario usuario,
                                  String campo, String anterior, String nuevo) {
        if (anterior != null && anterior.equals(nuevo)) return;
        BitacoraPaciente log = new BitacoraPaciente();
        log.setPaciente(paciente);
        log.setUsuario(usuario);
        log.setFechaHora(LocalDateTime.now());
        log.setAccion("edición");
        log.setCampoModificado(campo);
        log.setValorAnterior(anterior);
        log.setValorNuevo(nuevo);
        bitacoraRepository.save(log);
    }

    public List<BitacoraPaciente> obtenerBitacora(String numeroDocumento) {
        return bitacoraRepository
                .findByPaciente_NumeroDocumentoOrderByFechaHoraDesc(numeroDocumento);
    }
}