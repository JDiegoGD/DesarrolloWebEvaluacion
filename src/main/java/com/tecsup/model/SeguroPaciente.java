package com.tecsup.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "seguro_paciente")
public class SeguroPaciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSeguroPaciente;

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @Column(length = 50)  private String tipoSeguro;
    @Column(length = 100) private String empresaAseguradora;
    @Column(length = 50)  private String numeroPoliza;
    @Column(length = 50)  private String numeroAfiliacion;
    private LocalDate fechaInicio;
    private LocalDate fechaVencimiento;
    @Column(length = 20)  private String estadoCobertura;
}