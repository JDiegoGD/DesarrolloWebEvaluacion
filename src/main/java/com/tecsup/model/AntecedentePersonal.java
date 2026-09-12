package com.tecsup.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
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
}