package com.tecsup.model;

import jakarta.persistence.*;

@Entity
@Table(name = "alergia")
public class Alergia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAlergia;

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @Column(length = 50)  private String tipo; // Medicamento / alimento / otra
    @Column(length = 200) private String descripcion;
    @Column(length = 200) private String reaccion;

    public Integer getIdAlergia() {
        return idAlergia; }
    public void setIdAlergia(Integer idAlergia) {
        this.idAlergia = idAlergia; }

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

    public String getReaccion() {
        return reaccion; }
    public void setReaccion(String reaccion) {
        this.reaccion = reaccion; }
}