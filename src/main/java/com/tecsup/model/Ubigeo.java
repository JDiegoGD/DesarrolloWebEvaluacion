package com.tecsup.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "ubigeo")
public class Ubigeo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUbigeo;

    @NotBlank(message = "el distrito es obligatorio")
    @Column(nullable = false, length = 100) private String distrito;
    @NotBlank(message = "la provincia es obligatoria")
    @Column(nullable = false, length = 100) private String provincia;
    @NotBlank(message = "el departamento es obligatorio")
    @Column(nullable = false, length = 100) private String departamento;

    public Integer getIdUbigeo() { return idUbigeo; }
    public void setIdUbigeo(Integer idUbigeo) { this.idUbigeo = idUbigeo; }

    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
}