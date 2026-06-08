package com.utp.clinica;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

// @Entity indica que esta clase es una tabla en la BD
// @Table especifica el nombre exacto de la tabla en MySQL
@Entity
@Table(name = "pacientes")
@Data
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_paciente;

    @Column(nullable = false, unique = true)
    private String numero_historia_clinica;

    @Column(nullable = false, unique = true)
    private String dni;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    private Date fecha_nacimiento;
    private String sexo;
    private String telefono;
    private String direccion;
    private String correo;
    
    @Column(columnDefinition = "TEXT")
    private String alergias;

    private String estado;
}