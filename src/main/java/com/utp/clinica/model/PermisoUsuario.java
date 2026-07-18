package com.utp.clinica.model;

import jakarta.persistence.*;

/**
 * Entidad que almacena los permisos de módulos asignados a cada usuario.
 * Permite la delegación granular de acceso a las vistas del sistema.
 */
@Entity
@Table(name = "permisos_usuario")
public class PermisoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Integer idPermiso;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /**
     * Nombre del módulo al que el usuario tiene acceso.
     * Valores válidos: "dashboard", "citas", "pacientes", "farmacia", "horarios", "usuarios"
     */
    @Column(nullable = false, length = 50)
    private String modulo;

    // --- GETTERS Y SETTERS ---
    public Integer getIdPermiso() { return idPermiso; }
    public void setIdPermiso(Integer idPermiso) { this.idPermiso = idPermiso; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }
}
