package com.utp.clinica.controller;

import com.utp.clinica.model.*;
import com.utp.clinica.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador encargado de procesar peticiones y acciones de citas médicas
 */
@Controller
@RequestMapping("/citas")
public class CitaController {

    @Autowired private CitaService citaService;
    @Autowired private PacienteService pacienteService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private HorarioService horarioService;
    @Autowired private com.utp.clinica.repository.EspecialidadRepository especialidadRepo;
    @Autowired private com.utp.clinica.repository.ConsultorioRepository consultorioRepo;
    @Autowired private com.utp.clinica.repository.UsuarioRepository usuarioRepo;

    /** Duración estándar de cada turno de atención, en minutos */
    private static final int DURACION_TURNO_MINUTOS = 30;

    /**
     * Endpoint REST para obtener los médicos de una especialidad determinada
     * Usado por el JavaScript del modal de citas para filtrar dinámicamente
     */
    @GetMapping("/medicos-por-especialidad")
    @ResponseBody
    public ResponseEntity<?> medicosPorEspecialidad(@RequestParam("idEspecialidad") Integer idEspecialidad) {
        List<Usuario> medicos = usuarioRepo.findByRolAndEspecialidad_IdEspecialidad(Usuario.Rol.MEDICO, idEspecialidad);
        List<Map<String, Object>> resultado = medicos.stream()
                .filter(m -> m.getEstado()) // Solo médicos activos
                .map(m -> Map.<String, Object>of(
                        "idUsuario", m.getIdUsuario(),
                        "nombres", m.getNombres(),
                        "apellidos", m.getApellidos()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(resultado);
    }

    /**
     * Endpoint REST que devuelve los turnos de 30 minutos de un médico para una fecha,
     * marcando cuáles están LIBRE y cuáles OCUPADO (ya reservados). Alimenta el paso 2
     * del modal de agendamiento para que refleje el horario real del médico y las reservas.
     */
    @GetMapping("/horarios-disponibles")
    @ResponseBody
    public ResponseEntity<?> horariosDisponibles(@RequestParam("idMedico") Integer idMedico,
                                                 @RequestParam("fecha") String fechaStr) {
        try {
            LocalDate fecha = LocalDate.parse(fechaStr);
            Usuario medico = usuarioService.buscarPorId(idMedico)
                    .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));

            // 1. ¿La fecha cae dentro de un bloqueo (vacaciones/feriado)?
            String motivoBloqueo = horarioService.motivoBloqueoEnFecha(idMedico, fecha);
            if (motivoBloqueo != null) {
                return ResponseEntity.ok(Map.of(
                        "disponible", false,
                        "motivo", "El médico no atiende ese día (bloqueo: " + motivoBloqueo + ").",
                        "slots", List.of()));
            }

            // 2. Turnos fijos del médico para el día de la semana correspondiente
            String diaSemana = diaSemanaEnEspanol(fecha.getDayOfWeek());
            List<HorarioMedico> turnos = horarioService.obtenerHorarioPorMedicoYDia(idMedico, diaSemana);
            if (turnos.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "disponible", false,
                        "motivo", "El médico no tiene horario de atención el día " + diaSemana + ".",
                        "slots", List.of()));
            }

            // 3. Citas ya reservadas ese día (para marcar turnos ocupados)
            List<Cita> citasDelDia = citaService.obtenerCitasActivasDelDia(medico, fecha);
            List<LocalTime> horasOcupadas = citasDelDia.stream()
                    .map(c -> c.getFechaHora().toLocalTime())
                    .collect(Collectors.toList());

            LocalDateTime ahora = LocalDateTime.now();

            // 4. Generar los slots de 30 min dentro de cada turno del médico
            List<Map<String, Object>> slots = new ArrayList<>();
            for (HorarioMedico turno : turnos) {
                LocalTime cursor = turno.getHoraInicio();
                while (cursor.plusMinutes(DURACION_TURNO_MINUTOS).compareTo(turno.getHoraFin()) <= 0) {
                    String estado;
                    final LocalTime slot = cursor;
                    boolean ocupado = horasOcupadas.stream()
                            .anyMatch(h -> Math.abs(java.time.Duration.between(h, slot).toMinutes()) < DURACION_TURNO_MINUTOS);
                    boolean pasado = LocalDateTime.of(fecha, slot).isBefore(ahora);
                    if (ocupado) {
                        estado = "OCUPADO";
                    } else if (pasado) {
                        estado = "PASADO";
                    } else {
                        estado = "LIBRE";
                    }
                    slots.add(Map.of("hora", slot.toString().substring(0, 5), "estado", estado));
                    cursor = cursor.plusMinutes(DURACION_TURNO_MINUTOS);
                }
            }

            return ResponseEntity.ok(Map.of("disponible", true, "motivo", "", "slots", slots));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Traduce el día de la semana de Java al formato en español usado en los horarios (LUNES, MARTES...)
     */
    private String diaSemanaEnEspanol(DayOfWeek dow) {
        switch (dow) {
            case MONDAY: return "LUNES";
            case TUESDAY: return "MARTES";
            case WEDNESDAY: return "MIERCOLES";
            case THURSDAY: return "JUEVES";
            case FRIDAY: return "VIERNES";
            case SATURDAY: return "SABADO";
            default: return "DOMINGO";
        }
    }

    /**
     * Agendar una nueva cita (llamada AJAX/REST)
     */
    @PostMapping("/agendar")
    @ResponseBody
    public ResponseEntity<?> agendar(@RequestBody Map<String, Object> payload) {
        try {
            Integer idPaciente = Integer.valueOf(payload.get("idPaciente").toString());
            Integer idMedico = Integer.valueOf(payload.get("idMedico").toString());
            Integer idEspecialidad = Integer.valueOf(payload.get("idEspecialidad").toString());
            Integer idConsultorio = Integer.valueOf(payload.get("idConsultorio").toString());
            String fechaHoraStr = payload.get("fechaHora").toString(); // ISO format: yyyy-MM-dd'T'HH:mm:ss o yyyy-MM-dd HH:mm

            LocalDateTime fechaHora = LocalDateTime.parse(fechaHoraStr.replace(" ", "T"));

            Paciente paciente = pacienteService.buscarPorId(idPaciente)
                    .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
            Usuario medico = usuarioService.buscarPorId(idMedico)
                    .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));
            Especialidad especialidad = especialidadRepo.findById(idEspecialidad)
                    .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada"));
            Consultorio consultorio = consultorioRepo.findById(idConsultorio)
                    .orElseThrow(() -> new IllegalArgumentException("Consultorio no encontrado"));

            Cita cita = new Cita();
            cita.setPaciente(paciente);
            cita.setMedico(medico);
            cita.setEspecialidad(especialidad);
            cita.setConsultorio(consultorio);
            cita.setFechaHora(fechaHora);
            cita.setEstado(Cita.EstadoCita.PROGRAMADA);

            Cita guardada = citaService.agendarCita(cita);
            return ResponseEntity.ok(guardada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Cancelar una cita médica
     */
    @PostMapping("/cancelar/{idCita}")
    public String cancelarCita(@PathVariable("idCita") Integer idCita) {
        try {
            citaService.cambiarEstado(idCita, Cita.EstadoCita.CANCELADA);
        } catch (Exception e) {
            // Manejar error silenciosamente o registrar log
        }
        return "redirect:/citas";
    }

    /**
     * Confirmar una cita médica
     */
    @PostMapping("/confirmar/{idCita}")
    public String confirmarCita(@PathVariable("idCita") Integer idCita) {
        try {
            citaService.cambiarEstado(idCita, Cita.EstadoCita.CONFIRMADA);
        } catch (Exception e) {
            // Manejar error
        }
        return "redirect:/citas";
    }
}
