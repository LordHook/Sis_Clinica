package com.utp.clinica.repository;

import com.utp.clinica.model.PermisoUsuario;
import com.utp.clinica.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio JPA para gestionar los permisos de módulos por usuario
 */
@Repository
public interface PermisoUsuarioRepository extends JpaRepository<PermisoUsuario, Integer> {

    /**
     * Obtiene todos los permisos asignados a un usuario
     */
    List<PermisoUsuario> findByUsuario(Usuario usuario);

    /**
     * Elimina todos los permisos de un usuario (para reasignación)
     */
    void deleteByUsuario(Usuario usuario);
}
