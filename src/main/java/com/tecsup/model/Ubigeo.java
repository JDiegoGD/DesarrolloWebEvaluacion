package com.tecsup.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ubigeo")
public class Ubigeo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUbigeo;

    @Column(nullable = false, length = 100) private String distrito;
    @Column(nullable = false, length = 100) private String provincia;
    @Column(nullable = false, length = 100) private String departamento;
}