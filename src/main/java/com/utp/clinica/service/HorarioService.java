package com.utp.clinica.service;

import com.utp.clinica.model.BloqueoAgenda;
import com.utp.clinica.model.HorarioMedico;
import com.utp.clinica.model.Usuario;
import com.utp.clinica.repository.BloqueoAgendaRepository;
import com.utp.clinica.repository.HorarioMedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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
