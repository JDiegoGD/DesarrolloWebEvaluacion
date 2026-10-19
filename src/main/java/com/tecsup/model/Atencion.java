package com.tecsup.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "atencion")
public class Atencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAtencion;

    @NotNull(message = "debes indicar el paciente")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    // Médico que atiende; nullable para no romper las atenciones ya registradas
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_medico")
    private Usuario medico;

    // Id del médico recibido en el body; solo de entrada porque Usuario no expone su id en JSON
    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer idMedico;

    @NotNull(message = "la fecha de atención es obligatoria")
    @Column(nullable = false)
    private LocalDate fechaAtencion;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @JsonIgnore
    public Integer getIdAtencion() {
        return idAtencion; }
    public void setIdAtencion(Integer idAtencion) {
        this.idAtencion = idAtencion; }

    public Paciente getPaciente() {
        return paciente; }
    public void setPaciente(Paciente paciente) {
        this.paciente = paciente; }

    public Usuario getMedico() {
        return medico; }
    public void setMedico(Usuario medico) {
        this.medico = medico; }

    public Integer getIdMedico() {
        return idMedico; }
    public void setIdMedico(Integer idMedico) {
        this.idMedico = idMedico; }

    public LocalDate getFechaAtencion() {
        return fechaAtencion; }
    public void setFechaAtencion(LocalDate fechaAtencion) {
        this.fechaAtencion = fechaAtencion; }

    public String getMotivo() {
        return motivo; }
    public void setMotivo(String motivo) {
        this.motivo = motivo; }
}