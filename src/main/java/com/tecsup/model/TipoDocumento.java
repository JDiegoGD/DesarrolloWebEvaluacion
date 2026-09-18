package com.tecsup.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.List;


@Entity
@Table(name = "tipo_documento")
public class TipoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTipoDocumento;

    @NotBlank(message = "el código del tipo de documento es obligatorio")
    @Column(nullable = false, length = 10, unique = true)
    private String codigo;

    @Column(nullable = false, length = 50)
    private String nombre;

    @JsonIgnore
    @OneToMany(mappedBy = "tipoDocumento")
    private List<Paciente> pacientes;

    @JsonIgnore
    public Integer getIdTipoDocumento() { return idTipoDocumento; }
    public void setIdTipoDocumento(Integer idTipoDocumento) { this.idTipoDocumento = idTipoDocumento; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<Paciente> getPacientes() { return pacientes; }
    public void setPacientes(List<Paciente> pacientes) { this.pacientes = pacientes; }
}