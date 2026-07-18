package com.utp.clinica.model;

import jakarta.persistence.*;

/**
 * Entidad que representa un consultorio físico de la clínica (ej. Cons. 101, piso 1)
 */
@Entity
@Table(name = "consultorios")
public class Consultorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consultorio")
    private Integer idConsultorio;

    @Column(name = "nombre_numero", unique = true, nullable = false, length = 50)
    private String nombreNumero;

    @Column(length = 20)
    private String piso;

    // --- GETTERS Y SETTERS ---
    public Integer getIdConsultorio() { return idConsultorio; }
    public void setIdConsultorio(Integer idConsultorio) { this.idConsultorio = idConsultorio; }

    public String getNombreNumero() { return nombreNumero; }
    public void setNombreNumero(String nombreNumero) { this.nombreNumero = nombreNumero; }

    public String getPiso() { return piso; }
    public void setPiso(String piso) { this.piso = piso; }
}
