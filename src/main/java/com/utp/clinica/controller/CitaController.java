package com.utp.clinica.controller;

import com.utp.clinica.model.*;
import com.utp.clinica.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    @Autowired private com.utp.clinica.repository.EspecialidadRepository especialidadRepo;
    @Autowired private com.utp.clinica.repository.ConsultorioRepository consultorioRepo;
    @Autowired private com.utp.clinica.repository.UsuarioRepository usuarioRepo;

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
