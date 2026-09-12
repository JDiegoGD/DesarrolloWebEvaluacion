package com.tecsup.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "contacto_emergencia")
public class ContactoEmergencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idContacto;

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @Column(nullable = false, length = 150) private String nombreCompleto;
    @Column(length = 50)  private String parentesco;
    @Column(length = 20)  private String telefono;
    @Column(length = 200) private String direccion;
    @Column(length = 100) private String correoElectronico;
    @Column(nullable = false) private Boolean esPrincipal = false;
}