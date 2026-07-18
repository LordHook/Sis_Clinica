package com.utp.clinica.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa la consulta o acto médico derivado de una cita
 */
@Entity
@Table(name = "consultas_medicas")
public class ConsultaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consulta")
    private Integer idConsulta;

    @OneToOne
    @JoinColumn(name = "id_cita", nullable = false, unique = true)
    private Cita cita;

    @Column(name = "presion_arterial", length = 20)
    private String presionArterial;

    @Column(precision = 4, scale = 2)
    private BigDecimal temperatura;

    @Column(name = "peso_kg", precision = 5, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "talla_cm")
    private Integer tallaCm;

    @Column(name = "frecuencia_cardiaca")
    private Integer frecuenciaCardiaca;

    @Column(name = "motivo_consulta", nullable = false, columnDefinition = "TEXT")
    private String motivoConsulta;

<<<<<<< HEAD
    // NOTA: se amplía a texto libre (antes limitado a 10 caracteres para código CIE-10).
    // Aquí se usa como el campo "Diagnóstico" que redacta el médico.
    @Column(name = "diagnostico_cie10", columnDefinition = "TEXT")
    private String diagnosticoCie10;

    // Tratamiento indicado por el médico (nuevo campo, no afecta datos existentes)
    @Column(columnDefinition = "TEXT")
    private String tratamiento;

=======
    @Column(name = "diagnostico_cie10", length = 10)
    private String diagnosticoCie10;

>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a
    @Column(columnDefinition = "TEXT")
    private String evolucion;

    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }

    // --- GETTERS Y SETTERS ---
    public Integer getIdConsulta() { return idConsulta; }
    public void setIdConsulta(Integer idConsulta) { this.idConsulta = idConsulta; }

    public Cita getCita() { return cita; }
    public void setCita(Cita cita) { this.cita = cita; }

    public String getPresionArterial() { return presionArterial; }
    public void setPresionArterial(String presionArterial) { this.presionArterial = presionArterial; }

    public BigDecimal getTemperatura() { return temperatura; }
    public void setTemperatura(BigDecimal temperatura) { this.temperatura = temperatura; }

    public BigDecimal getPesoKg() { return pesoKg; }
    public void setPesoKg(BigDecimal pesoKg) { this.pesoKg = pesoKg; }

    public Integer getTallaCm() { return tallaCm; }
    public void setTallaCm(Integer tallaCm) { this.tallaCm = tallaCm; }

    public Integer getFrecuenciaCardiaca() { return frecuenciaCardiaca; }
    public void setFrecuenciaCardiaca(Integer frecuenciaCardiaca) { this.frecuenciaCardiaca = frecuenciaCardiaca; }

    public String getMotivoConsulta() { return motivoConsulta; }
    public void setMotivoConsulta(String motivoConsulta) { this.motivoConsulta = motivoConsulta; }

    public String getDiagnosticoCie10() { return diagnosticoCie10; }
    public void setDiagnosticoCie10(String diagnosticoCie10) { this.diagnosticoCie10 = diagnosticoCie10; }

<<<<<<< HEAD
    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }

=======
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a
    public String getEvolucion() { return evolucion; }
    public void setEvolucion(String evolucion) { this.evolucion = evolucion; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
