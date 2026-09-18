package com.tecsup.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Entity
@Table(name = "paciente")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPaciente;

    @Column(unique = true, length = 20)
    private String codigoPaciente;

    @ManyToOne
    @JoinColumn(name = "id_tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(unique = true, nullable = false, length = 20)
    private String numeroDocumento;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidoPaterno;

    @Column(nullable = false, length = 100)
    private String apellidoMaterno;

    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @Transient
    public Integer getEdad() {
        if (this.fechaNacimiento == null) return null;
        return Period.between(this.fechaNacimiento, LocalDate.now()).getYears();
    }

    @Column(nullable = false, length = 20)
    private String sexo;

    @Column(length = 30)  private String estadoCivil;
    @Column(length = 20)  private String telefono;
    @Column(length = 100) private String correoElectronico;
    @Column(length = 200) private String direccion;

    @ManyToOne
    @JoinColumn(name = "id_ubigeo")
    private Ubigeo ubigeo;

    @Column(length = 100) private String ocupacion;
    @Column(length = 5)   private String tipoSangre;

    @Column(nullable = false, length = 20)
    private String estado = "Activo";

    @Column(length = 300) private String fotografiaUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    public void prePersist() { this.fechaRegistro = LocalDateTime.now(); }

    @JsonIgnore
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    private List<ContactoEmergencia> contactosEmergencia;

    @JsonIgnore
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    private List<SeguroPaciente> seguros;

    @JsonIgnore
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    private List<Atencion> atenciones;

    @JsonIgnore
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    private List<AntecedentePersonal> antecedentesPersonales;

    @JsonIgnore
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    private List<AntecedenteFamiliar> antecedentesFamiliares;

    @JsonIgnore
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    private List<Alergia> alergias;

    @JsonIgnore
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    private List<BitacoraPaciente> bitacora;

    @JsonIgnore
    public Integer getIdPaciente() { return idPaciente; }
    public void setIdPaciente(Integer idPaciente) { this.idPaciente = idPaciente; }

    public String getCodigoPaciente() { return codigoPaciente; }
    public void setCodigoPaciente(String codigoPaciente) { this.codigoPaciente = codigoPaciente; }

    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getEstadoCivil() { return estadoCivil; }
    public void setEstadoCivil(String estadoCivil) { this.estadoCivil = estadoCivil; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Ubigeo getUbigeo() { return ubigeo; }
    public void setUbigeo(Ubigeo ubigeo) { this.ubigeo = ubigeo; }

    public String getOcupacion() { return ocupacion; }
    public void setOcupacion(String ocupacion) { this.ocupacion = ocupacion; }

    public String getTipoSangre() { return tipoSangre; }
    public void setTipoSangre(String tipoSangre) { this.tipoSangre = tipoSangre; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFotografiaUrl() { return fotografiaUrl; }
    public void setFotografiaUrl(String fotografiaUrl) { this.fotografiaUrl = fotografiaUrl; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }

    public List<ContactoEmergencia> getContactosEmergencia() { return contactosEmergencia; }
    public List<SeguroPaciente> getSeguros() { return seguros; }
    public List<Atencion> getAtenciones() { return atenciones; }
    public List<AntecedentePersonal> getAntecedentesPersonales() { return antecedentesPersonales; }
    public List<AntecedenteFamiliar> getAntecedentesFamiliares() { return antecedentesFamiliares; }
    public List<Alergia> getAlergias() { return alergias; }
    public List<BitacoraPaciente> getBitacora() { return bitacora; }
}