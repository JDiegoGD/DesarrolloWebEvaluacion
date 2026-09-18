package com.tecsup.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "atencion")
public class Atencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAtencion;

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @Column(nullable = false)
    private LocalDate fechaAtencion;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @JsonIgnore
    public Integer getIdAtencion() { return idAtencion; }
    public void setIdAtencion(Integer idAtencion) { this.idAtencion = idAtencion; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public LocalDate getFechaAtencion() { return fechaAtencion; }
    public void setFechaAtencion(LocalDate fechaAtencion) { this.fechaAtencion = fechaAtencion; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}