package com.utp.clinica;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "recetas")
@Data
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReceta;

    // Relación con la consulta médica que originó esta receta
    @ManyToOne
    @JoinColumn(name = "id_consulta", nullable = false)
    private ConsultaMedica consulta;

    // Relación con el medicamento recetado
    @ManyToOne
    @JoinColumn(name = "id_medicamento", nullable = false)
    private Medicamento medicamento;

    private String dosis;
    private String frecuencia;
    private int cantidad;

    // Métodos manuales 
public Medicamento getMedicamento() { return this.medicamento; }
public int getCantidad() { return this.cantidad; }
public void setEstado(String estado) { this.estado = estado; }
    // Estado: EMITIDA, DESPACHADA
    private String estado = "EMITIDA";
}