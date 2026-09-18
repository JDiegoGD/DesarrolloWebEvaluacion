package com.tecsup.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bitacora_paciente")
public class BitacoraPaciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idBitacora;

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(length = 50)
    private String accion;

    @Column(length = 100)
    private String campoModificado;

    @Column(columnDefinition = "TEXT")
    private String valorAnterior;

    @Column(columnDefinition = "TEXT")
    private String valorNuevo;

    // Getters y Setters
    @JsonIgnore
    public Integer getIdBitacora() {
        return idBitacora; }
    public void setIdBitacora(Integer idBitacora) {
        this.idBitacora = idBitacora; }

    public Paciente getPaciente() {
        return paciente; }
    public void setPaciente(Paciente paciente) {
        this.paciente = paciente; }

    public Usuario getUsuario() {
        return usuario; }
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario; }

    public LocalDateTime getFechaHora() {
        return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora; }

    public String getAccion() {
        return accion; }
    public void setAccion(String accion) {
        this.accion = accion; }

    public String getCampoModificado() {
        return campoModificado; }
    public void setCampoModificado(String campoModificado) {
        this.campoModificado = campoModificado; }

    public String getValorAnterior() {
        return valorAnterior; }
    public void setValorAnterior(String valorAnterior) {
        this.valorAnterior = valorAnterior; }

    public String getValorNuevo() {
        return valorNuevo; }
    public void setValorNuevo(String valorNuevo) {
        this.valorNuevo = valorNuevo; }
}