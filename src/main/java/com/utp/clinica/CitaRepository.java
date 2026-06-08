package com.utp.clinica;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    
    // Método para validar disponibilidad del médico en un horario específico
    List<Cita> findByMedicoAndFechaHora(Usuario medico, LocalDateTime fechaHora);
    
    // Método para listar todas las citas de un paciente
    List<Cita> findByPaciente(Paciente paciente);
}