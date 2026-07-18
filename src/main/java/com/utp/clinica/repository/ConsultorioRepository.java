package com.utp.clinica.repository;

import com.utp.clinica.model.Consultorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para realizar consultas sobre la entidad Consultorio
 */
@Repository
public interface ConsultorioRepository extends JpaRepository<Consultorio, Integer> {
}
