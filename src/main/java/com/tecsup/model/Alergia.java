package com.tecsup.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
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
}