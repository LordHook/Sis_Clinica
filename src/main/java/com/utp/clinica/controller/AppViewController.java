package com.utp.clinica.controller;

import com.utp.clinica.model.*;
import com.utp.clinica.service.*;
import org.springframework.beans.factory.annotation.Autowired;
<<<<<<< HEAD
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
=======
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.time.LocalDateTime;
<<<<<<< HEAD
=======
import java.time.LocalTime;
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a
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
<<<<<<< HEAD
     * Fecha de referencia para ubicar un despacho en el historial por días:
     * usa la fecha real de despacho si existe; si no (recetas antiguas), la fecha de la consulta.
     */
    private static LocalDateTime fechaReferenciaDespacho(Receta receta) {
        if (receta.getFechaDespacho() != null) {
            return receta.getFechaDespacho();
        }
        return receta.getConsulta().getFechaRegistro();
    }

    /**
     * Devuelve el Usuario actualmente autenticado, o null si no hay sesión válida.
     */
    private Usuario obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return null;
        }
        return usuarioService.buscarPorUsuario(auth.getName()).orElse(null);
    }

    /**
=======
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a
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
<<<<<<< HEAD
                model.addAttribute("idUsuarioActual", u.getIdUsuario());

                // Cargar permisos del usuario para el sidebar
                List<String> permisos = usuarioService.obtenerPermisos(u.getIdUsuario());
                if (permisos.isEmpty()) {
                    // Si no hay permisos guardados, usar los por defecto del rol
                    permisos = usuarioService.obtenerPermisosDefecto(u.getRol());
                }
                
                if (u.getRol() == Usuario.Rol.ADMINISTRADOR) {
                    // El administrador siempre tiene acceso a todos los módulos por seguridad
                    permisos = Arrays.asList("dashboard", "citas", "pacientes", "farmacia", "inventario", "horarios", "usuarios");
                }
                
                model.addAttribute("permisosUsuario", permisos);
=======

                // Cargar permisos del usuario para el sidebar
                if (u.getRol() == Usuario.Rol.ADMINISTRADOR) {
                    // El administrador siempre tiene acceso a todos los módulos
                    model.addAttribute("permisosUsuario", Arrays.asList("dashboard", "citas", "pacientes", "farmacia", "horarios", "usuarios"));
                } else {
                    List<String> permisos = usuarioService.obtenerPermisos(u.getIdUsuario());
                    if (permisos.isEmpty()) {
                        // Si no hay permisos guardados, usar los por defecto del rol
                        permisos = usuarioService.obtenerPermisosDefecto(u.getRol());
                    }
                    model.addAttribute("permisosUsuario", permisos);
                }
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a
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

    @GetMapping("/recuperar-password")
    public String mostrarRecuperacion(Model model) {
        return "recuperar-password";
    }

    @PostMapping("/recuperar-password")
    public String procesarRecuperacion(@RequestParam("correo") String correo,
                                       @RequestParam("dni") String dni,
                                       @RequestParam("nuevaContrasena") String nuevaContrasena,
                                       Model model) {
        boolean exito = usuarioService.restablecerContrasena(correo, dni, nuevaContrasena);
        if (exito) {
            return "redirect:/login?recoverySuccess=true";
        } else {
            model.addAttribute("error", "Los datos ingresados no coinciden con ningún registro.");
            return "recuperar-password";
        }
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

        // NUEVAS MÉTRICAS:
        // 1. Total Pacientes
        int totalPacientes = pacienteService.listarTodos().size();
        model.addAttribute("totalPacientes", totalPacientes);



        // 3. Eficiencia de Citas Atendidas
        long citasAtendidas = citasHoy.stream()
                .filter(c -> c.getEstado() == Cita.EstadoCita.ATENDIDA)
                .count();
        int eficiencia = citasHoy.isEmpty() ? 0 : (int) Math.round(((double) citasAtendidas / citasHoy.size()) * 100);
        model.addAttribute("citasAtendidas", citasAtendidas);
        model.addAttribute("eficienciaCitas", eficiencia);

        return "dashboard";
    }

    @GetMapping("/citas")
    public String citas(@RequestParam(value = "busqueda", required = false) String busqueda,
                        Model model) {
        cargarDatosUsuarioEnModelo(model);
        model.addAttribute("paginaActiva", "citas");
        model.addAttribute("tituloCabecera", "Módulo de Citas");

<<<<<<< HEAD
        // Determinar el usuario autenticado para saber si es un médico
        Usuario usuarioActual = obtenerUsuarioAutenticado();
        boolean esMedico = usuarioActual != null && usuarioActual.getRol() == Usuario.Rol.MEDICO;

        // Datos para las tablas.
        // Un MÉDICO solo ve SUS propias citas (atendidas anteriores, de hoy y agendadas a futuro).
        // Recepción/Administración ven todas las citas de la clínica.
        List<Cita> todasLasCitas = esMedico
                ? citaService.listarPorMedico(usuarioActual)
                : citaService.listarTodas();
=======
        // Datos para las tablas
        List<Cita> todasLasCitas = citaService.listarTodas();
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            String b = busqueda.toLowerCase();
            todasLasCitas = todasLasCitas.stream()
                    .filter(c -> c.getPaciente().getNombres().toLowerCase().contains(b)
                            || c.getPaciente().getApellidos().toLowerCase().contains(b)
                            || c.getPaciente().getDni().contains(b))
                    .collect(Collectors.toList());
        }
        model.addAttribute("citas", todasLasCitas);
<<<<<<< HEAD
        model.addAttribute("esVistaMedico", esMedico);
=======
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a

        // Combos del Modal de Registro
        model.addAttribute("pacientes", pacienteService.listarTodos());
        model.addAttribute("especialidades", especialidadRepo.findAll());
        model.addAttribute("consultorios", consultorioRepo.findAll());

        return "citas";
    }

    @GetMapping("/pacientes")
    public String pacientes(@RequestParam(value = "busqueda", required = false) String busqueda,
<<<<<<< HEAD
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size,
                            @RequestParam(value = "sort", defaultValue = "idPaciente") String sort,
                            @RequestParam(value = "dir", defaultValue = "desc") String dir,
=======
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a
                            Model model) {
        cargarDatosUsuarioEnModelo(model);
        model.addAttribute("paginaActiva", "pacientes");
        model.addAttribute("tituloCabecera", "Directorio de Pacientes");

<<<<<<< HEAD
        // Whitelist de columnas ordenables (evita pasar un nombre de propiedad inválido a Hibernate)
        Set<String> columnasValidas = Set.of("idPaciente", "numeroHistoriaClinica", "dni", "nombres", "apellidos", "fechaRegistro", "estado");
        String columnaOrden = columnasValidas.contains(sort) ? sort : "idPaciente";
        Sort.Direction direccion = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        int tamanoPagina = (size < 1 || size > 100) ? 10 : size;
        Pageable pageable = PageRequest.of(Math.max(page, 0), tamanoPagina, Sort.by(direccion, columnaOrden));

        Page<Paciente> pacientesPage = pacienteService.buscarPacientesPaginado(busqueda, pageable);
        model.addAttribute("pacientes", pacientesPage.getContent());
        model.addAttribute("pacientesPage", pacientesPage);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("sort", columnaOrden);
        model.addAttribute("dir", direccion == Sort.Direction.ASC ? "asc" : "desc");
        model.addAttribute("size", tamanoPagina);
=======
        List<Paciente> pacientes = pacienteService.buscarPacientes(busqueda);
        model.addAttribute("pacientes", pacientes);
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a

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
<<<<<<< HEAD

        // Historial de despachos agrupado por día (más reciente primero) para trazabilidad.
        // Si una receta antigua no tiene fecha de despacho registrada, se agrupa por la fecha de la consulta.
        Map<LocalDate, List<Receta>> despachosPorDia = recetasDespachadas.stream()
                .sorted(Comparator.comparing(AppViewController::fechaReferenciaDespacho).reversed())
                .collect(Collectors.groupingBy(
                        r -> fechaReferenciaDespacho(r).toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        model.addAttribute("recetas", recetasEmitidas);
        model.addAttribute("recetasDespachadas", recetasDespachadas);
        model.addAttribute("despachosPorDia", despachosPorDia);
=======
        
        model.addAttribute("recetas", recetasEmitidas);
        model.addAttribute("recetasDespachadas", recetasDespachadas);
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a

        return "farmacia";
    }

<<<<<<< HEAD
    @GetMapping("/inventario")
    public String inventario(@RequestParam(value = "busqueda", required = false) String busqueda,
                             Model model) {
        cargarDatosUsuarioEnModelo(model);
        model.addAttribute("paginaActiva", "inventario");
        model.addAttribute("tituloCabecera", "Inventario de Farmacia");

        List<Medicamento> medicamentos = farmaciaService.listarMedicamentos();
        if (busqueda != null && !busqueda.isEmpty()) {
            String q = busqueda.toLowerCase();
            medicamentos = medicamentos.stream()
                .filter(m -> m.getNombre().toLowerCase().contains(q))
                .collect(Collectors.toList());
        }

        model.addAttribute("medicamentos", medicamentos);
        return "inventario";
    }

=======
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a
    @GetMapping("/usuarios")
    public String usuarios(@RequestParam(value = "busqueda", required = false) String busqueda,
                           Model model) {
        cargarDatosUsuarioEnModelo(model);
        model.addAttribute("paginaActiva", "usuarios");
        model.addAttribute("tituloCabecera", "Gestión de Usuarios");

        List<Usuario> usuarios = usuarioService.buscarPersonal(busqueda);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("especialidades", especialidadRepo.findAll());

        // Calcular el ID del admin principal (el primer admin creado)
        Integer adminPrincipalId = usuarioService.listarPorRol(Usuario.Rol.ADMINISTRADOR).stream()
                .mapToInt(Usuario::getIdUsuario)
                .min().orElse(-1);
        model.addAttribute("adminPrincipalId", adminPrincipalId);

        return "usuarios";
    }

    @GetMapping("/horarios")
    public String horarios(@RequestParam(value = "medicoId", required = false) Integer medicoId,
                           Model model) {
        cargarDatosUsuarioEnModelo(model);
        model.addAttribute("paginaActiva", "horarios");
        model.addAttribute("tituloCabecera", "Horarios Médicos");

<<<<<<< HEAD
        Usuario usuarioActual = obtenerUsuarioAutenticado();
        boolean esMedico = usuarioActual != null && usuarioActual.getRol() == Usuario.Rol.MEDICO;

        List<Usuario> medicos;
        if (esMedico) {
            // El médico solo puede verse a sí mismo
            medicos = Arrays.asList(usuarioActual);
            medicoId = usuarioActual.getIdUsuario(); // Forzar su propio ID
        } else {
            medicos = usuarioService.listarPorRol(Usuario.Rol.MEDICO);
        }
        
        model.addAttribute("medicos", medicos);
        model.addAttribute("esVistaMedico", esMedico);
=======
        List<Usuario> medicos = usuarioService.listarPorRol(Usuario.Rol.MEDICO);
        model.addAttribute("medicos", medicos);
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a

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
