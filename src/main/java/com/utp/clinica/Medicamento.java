package com.utp.clinica;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "medicamentos")
@Data
public class Medicamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMedicamento;
    private String nombre;
    private int stock;
    private Double precio;

    // Métodos manuales
public int getStock() { return this.stock; }
public void setStock(int stock) { this.stock = stock; }
}