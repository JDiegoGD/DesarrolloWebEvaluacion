package com.tecsup.model;

import jakarta.persistence.*;

@Entity
@Table(name = "antecedente_familiar")
public class AntecedenteFamiliar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAntecedenteFamiliar;

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @Column(length = 50)  private String parentesco;
    @Column(length = 100) private String enfermedad;
    @Column(columnDefinition = "TEXT") private String observacion;

    public Integer getIdAntecedenteFamiliar() { return idAntecedenteFamiliar; }
    public void setIdAntecedenteFamiliar(Integer idAntecedenteFamiliar) { this.idAntecedenteFamiliar = idAntecedenteFamiliar; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public String getParentesco() { return parentesco; }
    public void setParentesco(String parentesco) { this.parentesco = parentesco; }

    public String getEnfermedad() { return enfermedad; }
    public void setEnfermedad(String enfermedad) { this.enfermedad = enfermedad; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}