package com.utp.clinica.controller;

import com.utp.clinica.model.*;
import com.utp.clinica.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador principal encargado de renderizar las vistas Thymeleaf del sistema
 */
@Controller
public class AppViewController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private PacienteService pacienteService;
    @Autowired private CitaService citaService;
    @Autowired private ConsultaMedicaService consultaService;
    @Autowired private FarmaciaService farmaciaService;
    @Autowired private HorarioService horarioService;

    @Autowired private com.utp.clinica.repository.EspecialidadRepository especialidadRepo;
    @Autowired private com.utp.clinica.repository.ConsultorioRepository consultorioRepo;

    /**
     * Helper para inyectar datos de sesión comunes en cada modelo de vista
     */
    private void cargarDatosUsuarioEnModelo(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            model.addAttribute("usuarioNombre", auth.getName());
            // Buscar en base de datos para obtener el nombre completo
            usuarioService.buscarPorUsuario(auth.getName()).ifPresent(u -> {
                model.addAttribute("nombreCompleto", u.getNombres() + " " + u.getApellidos());
                model.addAttribute("usuarioRol", u.getRol().name());
                model.addAttribute("usuarioSiglas", (u.getNombres().substring(0, 1) + u.getApellidos().substring(0, 1)).toUpperCase());
            });
        }
    }

    @GetMapping({"/", "/login"})
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        if (error != null) {
            model.addAttribute("mensajeError", "Usuario o contraseña incorrectos.");
        }
        if (logout != null) {
            model.addAttribute("mensajeSuccess", "Sesión cerrada correctamente.");
        }
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        cargarDatosUsuarioEnModelo(model);
        model.addAttribute("paginaActiva", "dashboard");
        model.addAttribute("tituloCabecera", "Dashboard");

        // Estadísticas Reales
        List<Cita> citasHoy = citaService.obtenerCitasDeHoy();
        model.addAttribute("totalCitasHoy", citasHoy.size());

        // Citas por Especialidad (agrupación en Java)
        Map<String, Long> especialidadesMap = citasHoy.stream()
                .collect(Collectors.groupingBy(c -> c.getEspecialidad().getNombre(), Collectors.counting()));
        model.addAttribute("citasPorEspecialidad", especialidadesMap);

        // Top Médicos (Conteo de citas hoy)
        Map<String, Long> topMedicosMap = citasHoy.stream()
                .collect(Collectors.groupingBy(c -> "Dr. " + c.getMedico().getApellidos(), Collectors.counting()));
        model.addAttribute("topMedicos", topMedicosMap);

        // Próximas Atenciones hoy (PROGRAMADAS o CONFIRMADAS)
        List<Cita> proximasAtenciones = citasHoy.stream()
                .filter(c -> c.getEstado() == Cita.EstadoCita.PROGRAMADA || c.getEstado() == Cita.EstadoCita.CONFIRMADA)
                .sorted(Comparator.comparing(Cita::getFechaHora))
                .collect(Collectors.toList());
        model.addAttribute("proximasAtenciones", proximasAtenciones);

        return "dashboard";
    }

    @GetMapping("/citas")
    public String citas(@RequestParam(value = "busqueda", required = false) String busqueda,
                        Model model) {
        cargarDatosUsuarioEnModelo(model);
        model.addAttribute("paginaActiva", "citas");
        model.addAttribute("tituloCabecera", "Módulo de Citas");

        // Datos para las tablas
        List<Cita> todasLasCitas = citaService.listarTodas();
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            String b = busqueda.toLowerCase();
            todasLasCitas = todasLasCitas.stream()
                    .filter(c -> c.getPaciente().getNombres().toLowerCase().contains(b)
                            || c.getPaciente().getApellidos().toLowerCase().contains(b)
                            || c.getPaciente().getDni().contains(b))
                    .collect(Collectors.toList());
        }
        model.addAttribute("citas", todasLasCitas);

        // Combos del Modal de Registro
        model.addAttribute("pacientes", pacienteService.listarTodos());
        model.addAttribute("medicos", usuarioService.listarPorRol(Usuario.Rol.MEDICO));
        model.addAttribute("especialidades", especialidadRepo.findAll());
        model.addAttribute("consultorios", consultorioRepo.findAll());

        return "citas";
    }

    @GetMapping("/pacientes")
    public String pacientes(@RequestParam(value = "busqueda", required = false) String busqueda,
                            Model model) {
        cargarDatosUsuarioEnModelo(model);
        model.addAttribute("paginaActiva", "pacientes");
        model.addAttribute("tituloCabecera", "Directorio de Pacientes");

        List<Paciente> pacientes = pacienteService.buscarPacientes(busqueda);
        model.addAttribute("pacientes", pacientes);

        return "pacientes";
    }

    @GetMapping("/ficha-paciente/{idPaciente}")
    public String fichaPaciente(@PathVariable("idPaciente") Integer idPaciente, Model model) {
        cargarDatosUsuarioEnModelo(model);
        model.addAttribute("paginaActiva", "pacientes");
        model.addAttribute("tituloCabecera", "Historia Clínica");

        Paciente paciente = pacienteService.buscarPorId(idPaciente)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
        model.addAttribute("paciente", paciente);

        // Edad calculada
        if (paciente.getFechaNacimiento() != null) {
            int edad = LocalDate.now().getYear() - paciente.getFechaNacimiento().getYear();
            model.addAttribute("edad", edad);
        } else {
            model.addAttribute("edad", 0);
        }

        // Historial de Consultas de Citas pasadas ATENDIDAS de este paciente
        List<Cita> citasPaciente = citaService.listarPorPaciente(paciente);
        List<ConsultaMedica> consultasPrevias = new ArrayList<>();
        Cita citaActivaDeHoy = null;

        for (Cita c : citasPaciente) {
            if (c.getEstado() == Cita.EstadoCita.ATENDIDA) {
                consultaService.buscarPorCita(c.getIdCita()).ifPresent(consultasPrevias::add);
            } else if ((c.getEstado() == Cita.EstadoCita.PROGRAMADA || c.getEstado() == Cita.EstadoCita.CONFIRMADA)
                    && c.getFechaHora().toLocalDate().equals(LocalDate.now())) {
                citaActivaDeHoy = c;
            }
        }
        
        // Ordenar consultas previas descendente por fecha
        consultasPrevias.sort(Comparator.comparing(ConsultaMedica::getFechaRegistro).reversed());

        model.addAttribute("consultasPrevias", consultasPrevias);
        model.addAttribute("citaActiva", citaActivaDeHoy);
        model.addAttribute("medicamentos", farmaciaService.listarMedicamentos());

        return "ficha-paciente";
    }

    @GetMapping("/farmacia")
    public String farmacia(@RequestParam(value = "busqueda", required = false) String busqueda,
                           Model model) {
        cargarDatosUsuarioEnModelo(model);
        model.addAttribute("paginaActiva", "farmacia");
        model.addAttribute("tituloCabecera", "Despacho Farmacéutico");

        List<Receta> recetasEmitidas = farmaciaService.buscarRecetas(busqueda, Receta.EstadoReceta.EMITIDA);
        List<Receta> recetasDespachadas = farmaciaService.buscarRecetas(busqueda, Receta.EstadoReceta.DESPACHADA);
        
        model.addAttribute("recetas", recetasEmitidas);
        model.addAttribute("recetasDespachadas", recetasDespachadas);

        return "farmacia";
    }

    @GetMapping("/usuarios")
    public String usuarios(@RequestParam(value = "busqueda", required = false) String busqueda,
                           Model model) {
        cargarDatosUsuarioEnModelo(model);
        model.addAttribute("paginaActiva", "usuarios");
        model.addAttribute("tituloCabecera", "Gestión de Usuarios");

        List<Usuario> usuarios = usuarioService.buscarPersonal(busqueda);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("especialidades", especialidadRepo.findAll());

        return "usuarios";
    }

    @GetMapping("/horarios")
    public String horarios(@RequestParam(value = "medicoId", required = false) Integer medicoId,
                           Model model) {
        cargarDatosUsuarioEnModelo(model);
        model.addAttribute("paginaActiva", "horarios");
        model.addAttribute("tituloCabecera", "Horarios Médicos");

        List<Usuario> medicos = usuarioService.listarPorRol(Usuario.Rol.MEDICO);
        model.addAttribute("medicos", medicos);

        List<HorarioMedico> horarios;
        List<BloqueoAgenda> bloqueos;
        Usuario medicoSeleccionado = null;

        if (medicoId != null) {
            horarios = horarioService.obtenerHorarioPorMedico(medicoId);
            bloqueos = horarioService.obtenerBloqueosPorMedico(medicoId);
            medicoSeleccionado = usuarioService.buscarPorId(medicoId).orElse(null);
        } else {
            horarios = horarioService.listarTodosLosHorarios();
            bloqueos = new ArrayList<>(); // Vacío o todos
        }

        model.addAttribute("horarios", horarios);
        model.addAttribute("bloqueos", bloqueos);
        model.addAttribute("medicoSeleccionado", medicoSeleccionado);
        model.addAttribute("medicoId", medicoId);

        return "horarios";
    }
}
