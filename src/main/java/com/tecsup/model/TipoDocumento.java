package com.tecsup.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "tipo_documento")
public class TipoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTipoDocumento;

    @Column(nullable = false, length = 50)
    private String nombre;

    @OneToMany(mappedBy = "tipoDocumento")
    private List<Paciente> pacientes;
}