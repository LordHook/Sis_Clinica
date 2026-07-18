package com.utp.clinica.service;

import com.utp.clinica.model.BloqueoAgenda;
import com.utp.clinica.model.HorarioMedico;
import com.utp.clinica.model.Usuario;
import com.utp.clinica.repository.BloqueoAgendaRepository;
import com.utp.clinica.repository.HorarioMedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
<<<<<<< HEAD
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
=======
import java.util.List;
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a

/**
 * Servicio encargado de gestionar la disponibilidad y los bloqueos excepcionales de agenda médica
 */
@Service
public class HorarioService {

    @Autowired
    private HorarioMedicoRepository horarioRepo;

    @Autowired
    private BloqueoAgendaRepository bloqueoRepo;

    /**
     * Lista todos los horarios semanales configurados en la clínica
     */
    public List<HorarioMedico> listarTodosLosHorarios() {
        return horarioRepo.findAll();
    }

    /**
     * Obtiene los horarios semanales asignados a un médico
     */
    public List<HorarioMedico> obtenerHorarioPorMedico(Integer idMedico) {
        return horarioRepo.findByMedicoIdUsuario(idMedico);
    }

    /**
<<<<<<< HEAD
     * Obtiene los turnos fijos de un médico para un día de la semana concreto (ej. LUNES)
     */
    public List<HorarioMedico> obtenerHorarioPorMedicoYDia(Integer idMedico, String diaSemana) {
        return horarioRepo.findByMedicoIdUsuario(idMedico).stream()
                .filter(h -> h.getDiaSemana() != null && h.getDiaSemana().equalsIgnoreCase(diaSemana))
                .collect(Collectors.toList());
    }

    /**
     * Indica si una fecha cae dentro de algún bloqueo de agenda (vacaciones, feriado) del médico.
     * Devuelve el motivo del bloqueo, o null si la fecha está libre de bloqueos.
     */
    public String motivoBloqueoEnFecha(Integer idMedico, LocalDate fecha) {
        return bloqueoRepo.findByMedicoIdUsuario(idMedico).stream()
                .filter(b -> !fecha.isBefore(b.getFechaInicio()) && !fecha.isAfter(b.getFechaFin()))
                .map(BloqueoAgenda::getMotivo)
                .findFirst()
                .orElse(null);
    }

    /**
=======
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a
     * Agrega un nuevo turno semanal fijo para un médico
     */
    @Transactional
    public HorarioMedico agregarHorario(HorarioMedico horario) {
        return horarioRepo.save(horario);
    }

    /**
     * Remueve un turno semanal de un médico
     */
    @Transactional
    public void eliminarHorario(Integer idHorario) {
        horarioRepo.deleteById(idHorario);
    }

    /**
     * Obtiene los bloqueos de agenda asociados a un médico
     */
    public List<BloqueoAgenda> obtenerBloqueosPorMedico(Integer idMedico) {
        return bloqueoRepo.findByMedicoIdUsuario(idMedico);
    }

    /**
     * Registra un bloqueo de agenda (vacación, descanso o feriado)
     */
    @Transactional
    public BloqueoAgenda registrarBloqueo(BloqueoAgenda bloqueo) {
        return bloqueoRepo.save(bloqueo);
    }

    /**
     * Elimina o remueve un bloqueo de agenda registrado
     */
    @Transactional
    public void eliminarBloqueo(Integer idBloqueo) {
        bloqueoRepo.deleteById(idBloqueo);
    }
}
