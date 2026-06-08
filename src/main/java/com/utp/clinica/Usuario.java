package com.utp.clinica;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_usuario;
    private String dni;
    private String nombres;
    private String apellidos;
    private String usuario;
    private String contrasena;
    private String rol;
    private Boolean estado;
    private Date fecha_creacion;
}