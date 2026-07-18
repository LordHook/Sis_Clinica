package com.utp.clinica.repository;

import com.utp.clinica.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para realizar consultas sobre la entidad Usuario
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Busca un usuario por su credencial de ingreso (username)
     * @param usuario Nombre de usuario institucional
     * @return Usuario envuelto en un Optional
     */
    Optional<Usuario> findByUsuario(String usuario);

    /**
     * Filtra usuarios por su rol en el sistema
     * @param rol Rol (ADMINISTRADOR, RECEPCIONISTA, MEDICO, FARMACEUTICO)
     * @return Lista de usuarios con el rol indicado
     */
    List<Usuario> findByRol(Usuario.Rol rol);

    /**
     * Búsqueda general de personal por DNI, nombres o apellidos
     */
    List<Usuario> findByDniContainingOrNombresContainingOrApellidosContaining(String dni, String nombres, String apellidos);

    /**
     * Busca usuarios por rol y especialidad (para filtrar médicos por especialidad en citas)
     */
    List<Usuario> findByRolAndEspecialidad_IdEspecialidad(Usuario.Rol rol, Integer idEspecialidad);
}
