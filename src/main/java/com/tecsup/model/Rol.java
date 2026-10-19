package com.tecsup.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rol")
public class Rol {

    public static final String ADMINISTRADOR = "ADMINISTRADOR";
    public static final String MEDICO = "MEDICO";
    public static final String RECEPCIONISTA = "RECEPCIONISTA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRol;

    @NotBlank(message = "el nombre del rol es obligatorio")
    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    private boolean activo = true;

    @JsonIgnore
    @OneToMany(mappedBy = "rol", fetch = FetchType.LAZY)
    private List<Usuario> usuarios = new ArrayList<>();

    @JsonIgnore
    public Integer getIdRol() {
        return idRol; }
    public void setIdRol(Integer idRol) {
        this.idRol = idRol; }

    public String getNombre() {
        return nombre; }
    public void setNombre(String nombre) {
        this.nombre = nombre; }

    public String getDescripcion() {
        return descripcion; }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion; }

    public boolean isActivo() {
        return activo; }
    public void setActivo(boolean activo) {
        this.activo = activo; }

    public List<Usuario> getUsuarios() {
        return usuarios; }
}
