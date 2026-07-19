package com.utp.clinica.service;

import com.utp.clinica.model.Cita;
import com.utp.clinica.model.Paciente;
import com.utp.clinica.model.Usuario;
import com.utp.clinica.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.stream.Collectors;

/**
 * Servicio encargado de gestionar el ciclo de vida de las citas médicas
 */
@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepo;

    /**
     * Lista todas las citas médicas registradas
     */
    public List<Cita> listarTodas() {
        return citaRepo.findAll();
    }

    /**
     * Busca citas de un paciente
     */
    public List<Cita> listarPorPaciente(Paciente paciente) {
        return citaRepo.findByPaciente(paciente);
    }


    public Page<Cita> buscarCitasPaginado(String busqueda, Cita.EstadoCita estado, Pageable pageable) {
        String query = (busqueda != null && !busqueda.trim().isEmpty()) ? busqueda.trim() : null;
        return citaRepo.buscarCitasFiltros(query, estado, pageable);
    }

    /**
     * Lista todas las citas asignadas a un médico (pasadas atendidas, de hoy y agendadas a futuro),
     * ordenadas cronológicamente ascendente.
     */
    public List<Cita> listarPorMedico(Usuario medico) {
        return citaRepo.findByMedico(medico).stream()
                .sorted((a, b) -> a.getFechaHora().compareTo(b.getFechaHora()))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las citas ACTIVAS (no canceladas) de un médico en una fecha determinada.
     * Se usa para calcular qué turnos ya están reservados al agendar.
     */
    public List<Cita> obtenerCitasActivasDelDia(Usuario medico, LocalDate fecha) {
        LocalDateTime inicio = LocalDateTime.of(fecha, LocalTime.MIN);
        LocalDateTime fin = LocalDateTime.of(fecha, LocalTime.MAX);
        return citaRepo.findByMedicoAndFechaHoraBetween(medico, inicio, fin).stream()
                .filter(c -> c.getEstado() != Cita.EstadoCita.CANCELADA)
                .collect(Collectors.toList());
    }

    /**
     * Busca una cita específica por su ID
     */
    public Optional<Cita> buscarPorId(Integer id) {
        return citaRepo.findById(id);
    }

    /**
     * Obtiene las citas del día de hoy
     */
    public List<Cita> obtenerCitasDeHoy() {
        LocalDateTime inicio = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime fin = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return citaRepo.findByFechaHoraBetween(inicio, fin);
    }

    /**
     * Obtiene las citas del día de hoy asignadas a un médico específico (para su agenda personal),
     * ordenadas cronológicamente.
     */
    public List<Cita> obtenerCitasDeHoyPorMedico(Usuario medico) {
        LocalDateTime inicio = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime fin = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return citaRepo.findByMedicoAndFechaHoraBetween(medico, inicio, fin).stream()
                .sorted((a, b) -> a.getFechaHora().compareTo(b.getFechaHora()))
                .collect(Collectors.toList());
    }

    /**
     * Agendar o reprogramar una cita médica con validaciones de choque horario
     */
    @Transactional
    public Cita agendarCita(Cita cita) {
        // Validación básica: evitar cruces de citas para el mismo médico a la misma hora
        LocalDateTime inicioBloque = cita.getFechaHora().minusMinutes(29);
        LocalDateTime finBloque = cita.getFechaHora().plusMinutes(29);

        List<Cita> citasConflicto = citaRepo.findByMedicoAndFechaHoraBetween(
                cita.getMedico(), inicioBloque, finBloque
        );

        // Si existe conflicto con otra cita activa (no cancelada), lanzamos una excepción
        boolean tieneConflicto = citasConflicto.stream()
                .anyMatch(c -> !c.getIdCita().equals(cita.getIdCita()) 
                              && c.getEstado() != Cita.EstadoCita.CANCELADA);

        if (tieneConflicto) {
            throw new IllegalStateException("El médico ya tiene una cita agendada en ese rango horario.");
        }

        return citaRepo.save(cita);
    }

    /**
     * Modifica el estado de una cita (ej. cambiar a CONFIRMADA, ATENDIDA, CANCELADA)
     */
    @Transactional
    public Cita cambiarEstado(Integer idCita, Cita.EstadoCita nuevoEstado) {
        Cita cita = citaRepo.findById(idCita)
                .orElseThrow(() -> new IllegalArgumentException("No existe la cita con ID: " + idCita));
        cita.setEstado(nuevoEstado);
        return citaRepo.save(cita);
    }
}
