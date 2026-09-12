package com.tecsup.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
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
}