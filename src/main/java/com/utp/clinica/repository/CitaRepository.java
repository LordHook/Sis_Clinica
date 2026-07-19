package com.utp.clinica.repository;

import com.utp.clinica.model.Cita;
import com.utp.clinica.model.Paciente;
import com.utp.clinica.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para realizar consultas sobre la entidad Cita
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {

    /**
     * Obtiene las citas de un médico específico dentro de un rango de tiempo
     */
    List<Cita> findByMedicoAndFechaHoraBetween(Usuario medico, LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene todas las citas de la clínica dentro de un rango de tiempo
     */
    List<Cita> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene el historial de citas de un paciente
     */
    List<Cita> findByPaciente(Paciente paciente);

    /**
     * Obtiene todas las citas asignadas a un médico (pasadas, de hoy y futuras)
     */
    List<Cita> findByMedico(Usuario medico);
    
    @Query("SELECT c FROM Cita c WHERE " +
           "(:estado IS NULL OR c.estado = :estado) AND " +
           "(:busqueda IS NULL OR LOWER(c.paciente.nombres) LIKE LOWER(CONCAT('%', CAST(:busqueda AS text), '%')) OR " +
           "LOWER(c.paciente.apellidos) LIKE LOWER(CONCAT('%', CAST(:busqueda AS text), '%')) OR " +
           "c.paciente.dni LIKE CONCAT('%', CAST(:busqueda AS text), '%'))")
    org.springframework.data.domain.Page<Cita> buscarCitasFiltros(
            @org.springframework.data.repository.query.Param("busqueda") String busqueda,
            @org.springframework.data.repository.query.Param("estado") Cita.EstadoCita estado,
            org.springframework.data.domain.Pageable pageable);
}
