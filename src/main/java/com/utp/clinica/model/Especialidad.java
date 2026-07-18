package com.utp.clinica.model;

import jakarta.persistence.*;

/**
 * Entidad que representa una especialidad médica en la clínica (ej: Pediatría, Medicina General)
 */
@Entity
@Table(name = "especialidades")
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_especialidad")
    private Integer idEspecialidad;

    @Column(unique = true, nullable = false, length = 100)
    private String nombre;

    // --- GETTERS Y SETTERS ---
    public Integer getIdEspecialidad() { return idEspecialidad; }
    public void setIdEspecialidad(Integer idEspecialidad) { this.idEspecialidad = idEspecialidad; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
