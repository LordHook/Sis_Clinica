package com.utp.clinica;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "consultorios")
@Data
public class Consultorio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConsultorio;
    
    private String nombreNumero;
    private String piso;
}