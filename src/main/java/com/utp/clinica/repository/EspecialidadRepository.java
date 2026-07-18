package com.utp.clinica.repository;

import com.utp.clinica.model.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para realizar consultas sobre la entidad Especialidad
 */
@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, Integer> {
}
