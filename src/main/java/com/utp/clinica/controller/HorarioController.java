package com.utp.clinica.controller;

import com.utp.clinica.model.BloqueoAgenda;
import com.utp.clinica.model.HorarioMedico;
import com.utp.clinica.model.Usuario;
import com.utp.clinica.service.HorarioService;
import com.utp.clinica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Controlador encargado de procesar peticiones relacionadas a los Horarios y Bloqueos Médicos
 */
@Controller
@RequestMapping("/horarios")
public class HorarioController {

    @Autowired private HorarioService horarioService;
    @Autowired private UsuarioService usuarioService;

    /**
     * Guarda o agrega un nuevo turno semanal fijo para un médico
     */
    @PostMapping("/guardar")
    public String guardar(@RequestParam("medicoId") Integer medicoId,
                          @RequestParam("diaSemana") String diaSemana,
                          @RequestParam("horaInicio") String horaInicio,
                          @RequestParam("horaFin") String horaFin) {

        Usuario medico = usuarioService.buscarPorId(medicoId)
                .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));

        HorarioMedico horario = new HorarioMedico();
        horario.setMedico(medico);
        horario.setDiaSemana(diaSemana.toUpperCase());
        horario.setHoraInicio(LocalTime.parse(horaInicio));
        horario.setHoraFin(LocalTime.parse(horaFin));

        horarioService.agregarHorario(horario);

        return "redirect:/horarios?medicoId=" + medicoId;
    }

    /**
     * Elimina un turno fijo semanal
     */
    @PostMapping("/eliminar/{idHorario}")
    public String eliminarHorario(@PathVariable("idHorario") Integer idHorario,
                                  @RequestParam("medicoId") Integer medicoId) {
        horarioService.eliminarHorario(idHorario);
        return "redirect:/horarios?medicoId=" + medicoId;
    }

    /**
     * Registra un bloqueo excepcional de la agenda
     */
    @PostMapping("/bloquear")
    public String bloquear(@RequestParam("medicoId") Integer medicoId,
                           @RequestParam("fechaInicio") String fechaInicio,
                           @RequestParam("fechaFin") String fechaFin,
                           @RequestParam("motivo") String motivo) {

        Usuario medico = usuarioService.buscarPorId(medicoId)
                .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));

        BloqueoAgenda bloqueo = new BloqueoAgenda();
        bloqueo.setMedico(medico);
        bloqueo.setFechaInicio(LocalDate.parse(fechaInicio));
        bloqueo.setFechaFin(LocalDate.parse(fechaFin));
        bloqueo.setMotivo(motivo);

        horarioService.registrarBloqueo(bloqueo);

        return "redirect:/horarios?medicoId=" + medicoId;
    }

    /**
     * Elimina un bloqueo registrado
     */
    @PostMapping("/eliminar-bloqueo/{idBloqueo}")
    public String eliminarBloqueo(@PathVariable("idBloqueo") Integer idBloqueo,
                                  @RequestParam("medicoId") Integer medicoId) {
        horarioService.eliminarBloqueo(idBloqueo);
        return "redirect:/horarios?medicoId=" + medicoId;
    }
}
