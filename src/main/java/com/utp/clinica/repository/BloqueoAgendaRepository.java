package com.utp.clinica.repository;

import com.utp.clinica.model.BloqueoAgenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio JPA para realizar consultas sobre la entidad BloqueoAgenda
 */
@Repository
public interface BloqueoAgendaRepository extends JpaRepository<BloqueoAgenda, Integer> {

    /**
     * Obtiene los bloqueos de agenda excepcionales registrados para un médico
     */
    List<BloqueoAgenda> findByMedicoIdUsuario(Integer idUsuario);
}
