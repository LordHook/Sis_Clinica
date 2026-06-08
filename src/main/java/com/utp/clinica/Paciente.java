package com.utp.clinica;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pacientes")
@Data // Esta etiqueta mágica de Lombok crea los get y set automáticamente
public class Paciente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPaciente;

    @Column(unique = true)
    private String numeroHistoriaClinica;

    @Column(unique = true, nullable = false)
    private String dni;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    private LocalDate fechaNacimiento;
    
    private String sexo; // 'M', 'F', 'OTRO'
    private String telefono;
    private String direccion;
    private String correo;
    
    @Column(columnDefinition = "TEXT")
    private String alergias;
    
    private String estado = "ACTIVO";
    
    private LocalDateTime fechaRegistro;

    // INTEGRACIÓN DE MÉTODOS MANUALES
public String getNumeroHistoriaClinica() { return numeroHistoriaClinica; }
public void setNumeroHistoriaClinica(String numeroHistoriaClinica) { this.numeroHistoriaClinica = numeroHistoriaClinica; }
public void setEstado(String estado) { this.estado = estado; }

    // Esto le dice a Spring Boot que asigne la fecha exacta justo antes de guardar en la BD
    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }
}