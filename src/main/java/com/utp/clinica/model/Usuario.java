package com.utp.clinica.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa a un usuario del sistema (Administrador, Recepcionista, Médico, Farmacéutico)
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(unique = true, nullable = false, length = 20)
    private String dni;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(unique = true, nullable = false, length = 50)
    private String usuario;

    @Column(nullable = false, length = 255)
    private String contrasena;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Rol rol;

    @Column(nullable = false)
    private Boolean estado = true;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne
    @JoinColumn(name = "id_especialidad")
    private Especialidad especialidad; // Opcional, solo aplicable para rol MEDICO

    @ManyToOne
    @JoinColumn(name = "id_consultorio")
    private Consultorio consultorio; // Opcional, asignado automáticamente a los médicos


    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = true;
        }
    }

    public enum Rol {
        ADMINISTRADOR,
        RECEPCIONISTA,
        MEDICO,
        FARMACEUTICO
    }

    // --- GETTERS Y SETTERS ---
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Especialidad getEspecialidad() { return especialidad; }
    public void setEspecialidad(Especialidad especialidad) { this.especialidad = especialidad; }

    public Consultorio getConsultorio() { return consultorio; }
    public void setConsultorio(Consultorio consultorio) { this.consultorio = consultorio; }
}
