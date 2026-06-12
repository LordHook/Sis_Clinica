package com.utp.clinica.repository;

import com.utp.clinica.model.HorarioMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio JPA para realizar consultas sobre la entidad HorarioMedico
 */
@Repository
public interface HorarioMedicoRepository extends JpaRepository<HorarioMedico, Integer> {

    /**
     * Obtiene el horario fijo semanal de un médico
     */
    List<HorarioMedico> findByMedicoIdUsuario(Integer idUsuario);
}
