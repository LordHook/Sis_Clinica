package com.utp.clinica.repository;

import com.utp.clinica.model.ConsultaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repositorio JPA para realizar consultas sobre la entidad ConsultaMedica
 */
@Repository
public interface ConsultaMedicaRepository extends JpaRepository<ConsultaMedica, Integer> {

    /**
     * Obtiene la consulta médica asociada a una cita específica
     */
    Optional<ConsultaMedica> findByCitaIdCita(Integer idCita);
}
