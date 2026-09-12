package com.tecsup.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
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

    @Column(length = 50)  private String accion;
    @Column(length = 100) private String campoModificado;
    @Column(columnDefinition = "TEXT") private String valorAnterior;
    @Column(columnDefinition = "TEXT") private String valorNuevo;
}