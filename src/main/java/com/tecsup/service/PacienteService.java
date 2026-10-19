package com.tecsup.service;

import com.tecsup.auditoria.Auditable;
import com.tecsup.enums.OperacionAuditoria;
import com.tecsup.auditoria.AuditoriaContexto;
import com.tecsup.dto.EliminacionPaciente;
import com.tecsup.exception.RecursoNoEncontradoException;
import com.tecsup.model.Alergia;
import com.tecsup.model.AntecedenteFamiliar;
import com.tecsup.model.AntecedentePersonal;
import com.tecsup.model.Paciente;
import com.tecsup.model.BitacoraPaciente;
import com.tecsup.model.TipoDocumento;
import com.tecsup.model.Usuario;
import com.tecsup.repository.PacienteRepository;
import com.tecsup.repository.BitacoraPacienteRepository;
import com.tecsup.repository.TipoDocumentoRepository;
import com.tecsup.repository.AtencionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class PacienteService {

    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private BitacoraPacienteRepository bitacoraRepository;
    @Autowired private AtencionRepository atencionRepository;
    @Autowired private TipoDocumentoRepository tipoDocumentoRepository;
    @Autowired private UsuarioActualService usuarioActualService;

    // RF-PAC-01 + RF-PAC-02
    @Auditable(entidad = "Paciente", operacion = OperacionAuditoria.REGISTRO,
            detalle = "'Se registró el paciente ' + #resultado.numeroDocumento + ' (' + #resultado.codigoPaciente + ')'")
    @Transactional
    public Paciente registrarPaciente(Paciente paciente) {
        if (pacienteRepository.existsByNumeroDocumento(paciente.getNumeroDocumento()))
            throw new RuntimeException("El número de documento ya está registrado.");
        if (paciente.getCorreoElectronico() != null && !paciente.getCorreoElectronico().isBlank()
                && pacienteRepository.existsByCorreoElectronico(paciente.getCorreoElectronico()))
            throw new RuntimeException("El correo electrónico ya está registrado.");
        if (paciente.getTelefono() != null && !paciente.getTelefono().isBlank()
                && pacienteRepository.existsByTelefono(paciente.getTelefono()))
            throw new RuntimeException("El teléfono ya está registrado.");
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
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado (documento " + numeroDocumento + ")."));
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
    @Transactional(readOnly = true)
    public List<Paciente> listarTodos() {
        return pacienteRepository.findAll();
    }

    // RF-PAC-08
    @Transactional
    // La bitácora registra siempre al usuario autenticado (no se acepta un usuario por parámetro)
    @Auditable(entidad = "Paciente", operacion = OperacionAuditoria.MODIFICACION,
            detalle = "'Se modificó el paciente ' + #numeroDocumento")
    public Paciente actualizarPaciente(String numeroDocumento, Paciente datos) {
        Paciente e = obtenerEntidadPorDocumento(numeroDocumento);
        Usuario u = usuarioActualService.obtener();

        String correoNuevo = datos.getCorreoElectronico();
        if (correoNuevo != null && !correoNuevo.isBlank() && !correoNuevo.equals(e.getCorreoElectronico())
                && pacienteRepository.existsByCorreoElectronico(correoNuevo))
            throw new RuntimeException("El correo electrónico ya está registrado por otro paciente.");

        String telefonoNuevo = datos.getTelefono();
        if (telefonoNuevo != null && !telefonoNuevo.isBlank() && !telefonoNuevo.equals(e.getTelefono())
                && pacienteRepository.existsByTelefono(telefonoNuevo))
            throw new RuntimeException("El teléfono ya está registrado por otro paciente.");

        // Los campos que cambian salen de la bitácora existente y se añaden al detalle de la auditoría
        List<String> cambiados = new ArrayList<>();
        if (registrarBitacora(e, u, "nombres", e.getNombres(), datos.getNombres())) cambiados.add("nombres");
        if (registrarBitacora(e, u, "telefono", e.getTelefono(), datos.getTelefono())) cambiados.add("telefono");
        if (registrarBitacora(e, u, "correoElectronico", e.getCorreoElectronico(), datos.getCorreoElectronico()))
            cambiados.add("correoElectronico");
        if (registrarBitacora(e, u, "estado", e.getEstado(), datos.getEstado())) cambiados.add("estado");
        AuditoriaContexto.anotar(cambiados.isEmpty()
                ? "Sin cambios en nombres, teléfono, correo ni estado"
                : "Campos modificados: " + String.join(", ", cambiados));
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

    // RF-PAC-13: con atenciones -> Inactivo; sin atenciones -> se borra y el cascade/orphanRemoval
    // elimina contactos, seguros, antecedentes y alergias (atenciones y bitácora no tienen cascade)
    @Auditable(entidad = "Paciente", operacion = OperacionAuditoria.ELIMINACION,
            detalle = "#resultado.bajaLogica ? 'Baja lógica del paciente ' + #numeroDocumento + ': pasó a Inactivo porque tiene atenciones' : 'Eliminación física del paciente ' + #numeroDocumento + ' (sin atenciones)'")
    @Transactional
    public EliminacionPaciente eliminarPaciente(String numeroDocumento) {
        Paciente p = obtenerEntidadPorDocumento(numeroDocumento);
        long cantidadAtenciones = atencionRepository.countByPacienteIdPaciente(p.getIdPaciente());
        boolean bajaLogica = cantidadAtenciones > 0;
        if (bajaLogica) {
            p.setEstado("Inactivo");
            pacienteRepository.save(p);
        } else {
            pacienteRepository.delete(p);
        }
        return new EliminacionPaciente(p.getIdPaciente(), p.getNumeroDocumento(), bajaLogica);
    }

    // RF-PAC-12: solo campos que cambiaron (null y "" se consideran iguales)
    // Devuelve true si el campo cambió y se registró
    public boolean registrarBitacora(Paciente paciente, Usuario usuario,
                                     String campo, String anterior, String nuevo) {
        if (Objects.equals(vacioANull(anterior), vacioANull(nuevo))) return false;
        BitacoraPaciente log = new BitacoraPaciente();
        log.setPaciente(paciente);
        log.setUsuario(usuario);
        log.setFechaHora(LocalDateTime.now());
        log.setAccion("edición");
        log.setCampoModificado(campo);
        log.setValorAnterior(anterior);
        log.setValorNuevo(nuevo);
        bitacoraRepository.save(log);
        return true;
    }

    private static String vacioANull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }

    @Transactional(readOnly = true)
    public List<BitacoraPaciente> obtenerBitacora(String numeroDocumento) {
        return bitacoraRepository
                .findByPaciente_NumeroDocumentoOrderByFechaHoraDesc(numeroDocumento);
    }

    // Antecedentes y alergias: se agregan por el helper y se guardan por cascade
    @Auditable(entidad = "AntecedentePersonal", operacion = OperacionAuditoria.REGISTRO,
            detalle = "'Se registró un antecedente personal del paciente ' + #numeroDocumento")
    @Transactional
    public AntecedentePersonal registrarAntecedentePersonal(String numeroDocumento, AntecedentePersonal a) {
        obtenerEntidadPorDocumento(numeroDocumento).addAntecedentePersonal(a);
        return a;
    }

    @Transactional(readOnly = true)
    public List<AntecedentePersonal> listarAntecedentesPersonales(String numeroDocumento) {
        return List.copyOf(obtenerEntidadPorDocumento(numeroDocumento).getAntecedentesPersonales());
    }

    @Auditable(entidad = "AntecedenteFamiliar", operacion = OperacionAuditoria.REGISTRO,
            detalle = "'Se registró un antecedente familiar del paciente ' + #numeroDocumento")
    @Transactional
    public AntecedenteFamiliar registrarAntecedenteFamiliar(String numeroDocumento, AntecedenteFamiliar a) {
        obtenerEntidadPorDocumento(numeroDocumento).addAntecedenteFamiliar(a);
        return a;
    }

    @Transactional(readOnly = true)
    public List<AntecedenteFamiliar> listarAntecedentesFamiliares(String numeroDocumento) {
        return List.copyOf(obtenerEntidadPorDocumento(numeroDocumento).getAntecedentesFamiliares());
    }

    @Auditable(entidad = "Alergia", operacion = OperacionAuditoria.REGISTRO,
            detalle = "'Se registró una alergia del paciente ' + #numeroDocumento")
    @Transactional
    public Alergia registrarAlergia(String numeroDocumento, Alergia a) {
        obtenerEntidadPorDocumento(numeroDocumento).addAlergia(a);
        return a;
    }

    @Transactional(readOnly = true)
    public List<Alergia> listarAlergias(String numeroDocumento) {
        return List.copyOf(obtenerEntidadPorDocumento(numeroDocumento).getAlergias());
    }
}
