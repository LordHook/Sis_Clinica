package com.utp.clinica.model;

import jakarta.persistence.*;
<<<<<<< HEAD
import java.time.LocalDateTime;
=======
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a

/**
 * Entidad que representa la receta de un medicamento emitida en una consulta médica
 */
@Entity
@Table(name = "recetas")
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receta")
    private Integer idReceta;

    @ManyToOne
    @JoinColumn(name = "id_consulta", nullable = false)
    private ConsultaMedica consulta;

    @ManyToOne
    @JoinColumn(name = "id_medicamento", nullable = false)
    private Medicamento medicamento;

    @Column(length = 100)
    private String dosis;

    @Column(length = 100)
    private String frecuencia;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoReceta estado = EstadoReceta.EMITIDA;

<<<<<<< HEAD
    // Fecha y hora en que el farmacéutico despachó la receta (null mientras esté EMITIDA).
    // Permite construir el historial de despachos agrupado por día.
    @Column(name = "fecha_despacho")
    private LocalDateTime fechaDespacho;

=======
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a
    public enum EstadoReceta {
        EMITIDA,
        DESPACHADA
    }

    // --- GETTERS Y SETTERS ---
    public Integer getIdReceta() { return idReceta; }
    public void setIdReceta(Integer idReceta) { this.idReceta = idReceta; }

    public ConsultaMedica getConsulta() { return consulta; }
    public void setConsulta(ConsultaMedica consulta) { this.consulta = consulta; }

    public Medicamento getMedicamento() { return medicamento; }
    public void setMedicamento(Medicamento medicamento) { this.medicamento = medicamento; }

    public String getDosis() { return dosis; }
    public void setDosis(String dosis) { this.dosis = dosis; }

    public String getFrecuencia() { return frecuencia; }
    public void setFrecuencia(String frecuencia) { this.frecuencia = frecuencia; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public EstadoReceta getEstado() { return estado; }
    public void setEstado(EstadoReceta estado) { this.estado = estado; }
<<<<<<< HEAD

    public LocalDateTime getFechaDespacho() { return fechaDespacho; }
    public void setFechaDespacho(LocalDateTime fechaDespacho) { this.fechaDespacho = fechaDespacho; }
=======
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a
}
