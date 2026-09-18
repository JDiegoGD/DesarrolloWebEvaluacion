package com.tecsup.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "antecedente_personal")
public class AntecedentePersonal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAntecedentePersonal;

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @Column(length = 50)
    private String tipo; // Enfermedad previa / cirugía / hospitalización / crónica

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private LocalDate fecha;

    public Integer getIdAntecedentePersonal() {
        return idAntecedentePersonal; }
    public void setIdAntecedentePersonal(Integer idAntecedentePersonal) {
        this.idAntecedentePersonal = idAntecedentePersonal; }

    public Paciente getPaciente() {
        return paciente; }
    public void setPaciente(Paciente paciente) {
        this.paciente = paciente; }

    public String getTipo() {
        return tipo; }
    public void setTipo(String tipo) {
        this.tipo = tipo; }

    public String getDescripcion() {
        return descripcion; }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion; }

    public LocalDate getFecha() {
        return fecha; }
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha; }
}