package com.utp.clinica;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "consultas_medicas")
@Data
public class ConsultaMedica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConsulta;
    // ... otros campos
}