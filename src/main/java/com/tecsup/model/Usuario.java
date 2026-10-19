package com.tecsup.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;

    @NotBlank(message = "el nombre de usuario es obligatorio")
    @Column(nullable = false, unique = true, length = 100)
    private String nombreUsuario;

    // FK id_rol creada por docs/migracion_tarea2.sql (Hibernate no puede en MariaDB 10.4)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    // Hash BCrypt; nunca se serializa
    @JsonIgnore
    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 150)
    private String nombres;

    @Column(unique = true, length = 150)
    private String correo;

    @Column(nullable = false)
    private boolean activo = true;


    @JsonIgnore
    @OneToMany(mappedBy = "medico", fetch = FetchType.LAZY)
    private List<Atencion> atencionesAtendidas = new ArrayList<>();

    @JsonIgnore
    public Integer getIdUsuario() {
        return idUsuario; }
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario; }

    public String getNombreUsuario() {
        return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario; }

    @JsonIgnore
    public Rol getRol() {
        return rol; }
    public void setRol(Rol rol) {
        this.rol = rol; }

    // Mantiene el JSON anterior ("rol": "MEDICO") donde se serializa la entidad (bitácora, atenciones)
    @JsonProperty("rol")
    public String getNombreRol() {
        return rol == null ? null : rol.getNombre(); }

    @JsonIgnore
    public String getPassword() {
        return password; }
    public void setPassword(String password) {
        this.password = password; }

    public String getNombres() {
        return nombres; }
    public void setNombres(String nombres) {
        this.nombres = nombres; }

    public String getCorreo() {
        return correo; }
    public void setCorreo(String correo) {
        this.correo = correo; }

    public boolean isActivo() {
        return activo; }
    public void setActivo(boolean activo) {
        this.activo = activo; }

    public List<Atencion> getAtencionesAtendidas() {
        return atencionesAtendidas; }
}
