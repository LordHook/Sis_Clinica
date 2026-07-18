package com.utp.clinica.controller;

import com.utp.clinica.model.*;
import com.utp.clinica.repository.MedicamentoRepository;
import com.utp.clinica.repository.RecetaRepository;
import com.utp.clinica.service.CitaService;
import com.utp.clinica.service.ConsultaMedicaService;
import com.utp.clinica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de la Agenda Personal del Médico.
 * Permite al médico ver únicamente SUS citas del día y registrar el acto médico
 * (diagnóstico, tratamiento, receta) de forma vinculada a la cita agendada.
 * Toda la información del paciente se muestra en modo solo lectura (no editable).
 */
@Controller
public class MedicoController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private CitaService citaService;
    @Autowired private ConsultaMedicaService consultaMedicaService;
    @Autowired private MedicamentoRepository medicamentoRepo;
    @Autowired private RecetaRepository recetaRepo;

    /**
     * Obtiene el Usuario (médico) actualmente autenticado en la sesión
     */
    private Optional<Usuario> obtenerMedicoAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return Optional.empty();
        }
        return usuarioService.buscarPorUsuario(auth.getName());
    }

    /**
     * Carga los datos comunes de cabecera/sidebar (nombre, rol, siglas, permisos)
     * igual que en el resto del sistema, para que el layout se vea consistente.
     */
    private void cargarDatosComunes(Model model, Usuario medico) {
        model.addAttribute("usuarioNombre", medico.getUsuario());
        model.addAttribute("nombreCompleto", medico.getNombres() + " " + medico.getApellidos());
        model.addAttribute("usuarioRol", medico.getRol().name());
        model.addAttribute("usuarioSiglas", (medico.getNombres().substring(0, 1) + medico.getApellidos().substring(0, 1)).toUpperCase());

        if (medico.getRol() == Usuario.Rol.ADMINISTRADOR) {
            model.addAttribute("permisosUsuario", Arrays.asList("dashboard", "citas", "pacientes", "farmacia", "horarios", "usuarios"));
        } else if (medico.getRol() == Usuario.Rol.MEDICO) {
            // El médico solo tiene acceso al módulo de Citas
            model.addAttribute("permisosUsuario", Arrays.asList("citas"));
        } else {
            List<String> permisos = usuarioService.obtenerPermisos(medico.getIdUsuario());
            if (permisos.isEmpty()) {
                permisos = usuarioService.obtenerPermisosDefecto(medico.getRol());
            }
            model.addAttribute("permisosUsuario", permisos);
        }
    }

    /**
     * La antigua "Mi Agenda de Hoy" se retiró: las citas del médico (de hoy, futuras y
     * atendidas anteriores) ahora se ven directamente en el módulo de Citas.
     * Se mantiene la ruta redirigiendo para no romper enlaces antiguos.
     */
    @GetMapping("/medico/agenda")
    public String miAgenda() {
        return "redirect:/citas";
    }

    /**
     * Vista de consulta médica de una cita puntual.
     * - Muestra los datos del paciente EN SOLO LECTURA (no se pueden editar aquí).
     * - Si la cita aún no fue atendida: muestra el formulario para registrar
     *   diagnóstico, tratamiento, indicación adicional y medicinas recetadas.
     * - Si ya fue atendida: muestra en solo lectura lo que el médico ya registró.
     */
    @GetMapping("/medico/consulta/{idCita}")
    public String verConsulta(@PathVariable Integer idCita, Model model, RedirectAttributes redirectAttributes) {
        Optional<Usuario> medicoOpt = obtenerMedicoAutenticado();
        if (medicoOpt.isEmpty()) {
            return "redirect:/login";
        }
        Usuario medico = medicoOpt.get();

        Optional<Cita> citaOpt = citaService.buscarPorId(idCita);
        if (citaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "La cita indicada no existe.");
            return "redirect:/citas";
        }
        Cita cita = citaOpt.get();

        // Seguridad adicional: un médico (rol MEDICO) solo puede ver SUS propias citas.
        // Un administrador puede revisar cualquiera con fines de supervisión (solo lectura).
        boolean esSuPropiaCita = cita.getMedico() != null && cita.getMedico().getIdUsuario().equals(medico.getIdUsuario());
        if (medico.getRol() == Usuario.Rol.MEDICO && !esSuPropiaCita) {
            redirectAttributes.addFlashAttribute("mensajeError", "No puedes acceder a la consulta de otro médico.");
            return "redirect:/citas";
        }

        cargarDatosComunes(model, medico);
        model.addAttribute("paginaActiva", "citas");
        model.addAttribute("tituloCabecera", "Consulta Médica");

        model.addAttribute("cita", cita);
        model.addAttribute("paciente", cita.getPaciente());
        model.addAttribute("medicamentos", medicamentoRepo.findAll());

        Optional<ConsultaMedica> consultaOpt = consultaMedicaService.buscarPorCita(idCita);
        model.addAttribute("consultaExistente", consultaOpt.orElse(null));
        model.addAttribute("yaAtendida", consultaOpt.isPresent());

        if (consultaOpt.isPresent()) {
            model.addAttribute("recetasEmitidas", recetaRepo.findByConsultaIdConsulta(consultaOpt.get().getIdConsulta()));
        }

        return "consulta-medica";
    }
}
