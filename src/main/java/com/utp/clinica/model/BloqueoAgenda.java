package com.utp.clinica.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidad que representa bloqueos excepcionales en la agenda de un médico (vacaciones, permisos, feriados)
 */
@Entity
@Table(name = "bloqueos_agenda")
public class BloqueoAgenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bloqueo")
    private Integer idBloqueo;

    @ManyToOne
    @JoinColumn(name = "id_medico", nullable = false)
    private Usuario medico;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false, length = 200)
    private String motivo;

    // --- GETTERS Y SETTERS ---
    public Integer getIdBloqueo() { return idBloqueo; }
    public void setIdBloqueo(Integer idBloqueo) { this.idBloqueo = idBloqueo; }

    public Usuario getMedico() { return medico; }
    public void setMedico(Usuario medico) { this.medico = medico; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
