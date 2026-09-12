package com.tecsup.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Data
@Entity
@Table(name = "paciente")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPaciente;

    @Column(unique = true, length = 20)
    private String codigoPaciente;

    // RF-PAC-02: número de documento único
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

    // RF-PAC-13: estado lógico en vez de borrado físico
    @Column(nullable = false, length = 20)
    private String estado = "Activo";

    @Column(length = 300) private String fotografiaUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    public void prePersist() { this.fechaRegistro = LocalDateTime.now(); }

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    private List<ContactoEmergencia> contactosEmergencia;
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    private List<SeguroPaciente> seguros;
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    private List<Atencion> atenciones;
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    private List<BitacoraPaciente> bitacora;