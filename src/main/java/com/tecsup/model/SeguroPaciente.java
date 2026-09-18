package com.tecsup.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "seguro_paciente")
public class SeguroPaciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSeguroPaciente;

    @NotNull(message = "debes indicar el paciente")
    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @NotBlank(message = "el tipo de seguro es obligatorio")
    @Column(length = 50)  private String tipoSeguro;
    @Column(length = 100) private String empresaAseguradora;
    @Column(length = 50)  private String numeroPoliza;
    @Column(length = 50)  private String numeroAfiliacion;
    private LocalDate fechaInicio;
    private LocalDate fechaVencimiento;
    @Column(length = 20)  private String estadoCobertura;

    @JsonIgnore
    public Integer getIdSeguroPaciente() { return idSeguroPaciente; }
    public void setIdSeguroPaciente(Integer id) { this.idSeguroPaciente = id; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public String getTipoSeguro() { return tipoSeguro; }
    public void setTipoSeguro(String tipoSeguro) { this.tipoSeguro = tipoSeguro; }

    public String getEmpresaAseguradora() { return empresaAseguradora; }
    public void setEmpresaAseguradora(String empresaAseguradora) { this.empresaAseguradora = empresaAseguradora; }

    public String getNumeroPoliza() { return numeroPoliza; }
    public void setNumeroPoliza(String numeroPoliza) { this.numeroPoliza = numeroPoliza; }

    public String getNumeroAfiliacion() { return numeroAfiliacion; }
    public void setNumeroAfiliacion(String numeroAfiliacion) { this.numeroAfiliacion = numeroAfiliacion; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public String getEstadoCobertura() { return estadoCobertura; }
    public void setEstadoCobertura(String estadoCobertura) { this.estadoCobertura = estadoCobertura; }
}