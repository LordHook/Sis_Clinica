package com.utp.clinica.controller;

import com.utp.clinica.model.Paciente;
import com.utp.clinica.service.PacienteService;
<<<<<<< HEAD
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
=======
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a

/**
 * Controlador encargado de procesar las peticiones y altas de pacientes
 */
@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

<<<<<<< HEAD
    // DNI: solo números, entre 8 y 9 dígitos
    private static final Pattern PATRON_DNI = Pattern.compile("^\\d{8,9}$");
    // Teléfono: solo números, exactamente 9 dígitos
    private static final Pattern PATRON_TELEFONO = Pattern.compile("^\\d{9}$");
    // Nombres/Apellidos: solo letras (incluye tildes y Ñ) y espacios, sin números ni símbolos
    private static final Pattern PATRON_SOLO_LETRAS = Pattern.compile("^[A-ZÑÁÉÍÓÚÜ ]+$");

=======
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a
    /**
     * Guarda o actualiza un expediente de paciente (desde Formulario HTML)
     */
    @PostMapping("/guardar")
    public String guardar(@RequestParam(value = "idPaciente", required = false) Integer idPaciente,
                          @RequestParam("dni") String dni,
                          @RequestParam("nombres") String nombres,
                          @RequestParam("apellidos") String apellidos,
                          @RequestParam("fechaNacimiento") String fechaNacimiento,
                          @RequestParam("sexo") String sexo,
                          @RequestParam("telefono") String telefono,
                          @RequestParam("direccion") String direccion,
                          @RequestParam(value = "correo", required = false) String correo,
<<<<<<< HEAD
                          @RequestParam(value = "alergias", required = false) String alergias,
                          RedirectAttributes redirectAttributes) {

        // Validación de DNI (8 a 9 dígitos numéricos) - aplica tanto a registro como a edición
        if (dni == null || !PATRON_DNI.matcher(dni.trim()).matches()) {
            redirectAttributes.addFlashAttribute("mensajeError", "El DNI debe contener entre 8 y 9 dígitos numéricos.");
            return "redirect:/pacientes";
        }

        // Validación de Teléfono (exactamente 9 dígitos numéricos) - aplica tanto a registro como a edición
        if (telefono == null || !PATRON_TELEFONO.matcher(telefono.trim()).matches()) {
            redirectAttributes.addFlashAttribute("mensajeError", "El teléfono debe contener exactamente 9 dígitos numéricos.");
            return "redirect:/pacientes";
        }

        // Normalizar a mayúsculas (por si el navegador no aplicó la conversión, ej. envíos directos por API)
        nombres = nombres == null ? "" : nombres.trim().toUpperCase();
        apellidos = apellidos == null ? "" : apellidos.trim().toUpperCase();

        // Validación de Nombres y Apellidos (solo letras en mayúscula, sin números ni símbolos)
        if (!PATRON_SOLO_LETRAS.matcher(nombres).matches()) {
            redirectAttributes.addFlashAttribute("mensajeError", "El nombre solo debe contener letras en mayúscula, sin números.");
            return "redirect:/pacientes";
        }
        if (!PATRON_SOLO_LETRAS.matcher(apellidos).matches()) {
            redirectAttributes.addFlashAttribute("mensajeError", "El apellido solo debe contener letras en mayúscula, sin números.");
            return "redirect:/pacientes";
        }

=======
                          @RequestParam(value = "alergias", required = false) String alergias) {
        
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a
        Paciente paciente = new Paciente();
        if (idPaciente != null) {
            paciente.setIdPaciente(idPaciente);
        }
        paciente.setDni(dni);
        paciente.setNombres(nombres);
        paciente.setApellidos(apellidos);
        if (fechaNacimiento != null && !fechaNacimiento.isEmpty()) {
            paciente.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
        }
        paciente.setSexo(Paciente.Sexo.valueOf(sexo));
        paciente.setTelefono(telefono);
        paciente.setDireccion(direccion);
        paciente.setCorreo(correo);
        paciente.setAlergias(alergias);

<<<<<<< HEAD
        try {
            pacienteService.guardar(paciente);
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Ya existe un paciente registrado con ese DNI.");
            return "redirect:/pacientes";
        }

        redirectAttributes.addFlashAttribute("mensajeSuccess", "Paciente guardado correctamente.");
        return "redirect:/pacientes";
    }

    /**
     * Verificación en vivo (AJAX) de si un DNI ya está registrado.
     * Se llama al perder el foco (blur) del campo DNI en el formulario.
     * Si idPaciente viene informado (modo edición), se excluye ese registro de la comprobación.
     */
    @GetMapping("/verificar-dni")
    @ResponseBody
    public Map<String, Boolean> verificarDni(@RequestParam("dni") String dni,
                                              @RequestParam(value = "idPaciente", required = false) Integer idPaciente) {
        if (dni == null || !PATRON_DNI.matcher(dni.trim()).matches()) {
            // Si ni siquiera tiene el formato correcto, no tiene sentido consultar la BD
            return Collections.singletonMap("existe", false);
        }
        boolean existe = pacienteService.existeDni(dni.trim(), idPaciente);
        return Collections.singletonMap("existe", existe);
    }

    /**
     * Cambia el estado de un paciente entre ACTIVO e INACTIVO únicamente
     * (el estado FALLECIDO no se gestiona desde este módulo).
     */
    @PostMapping("/cambiar-estado")
    public String cambiarEstado(@RequestParam("idPaciente") Integer idPaciente,
                                 @RequestParam("nuevoEstado") String nuevoEstado,
                                 RedirectAttributes redirectAttributes) {
        try {
            Paciente.EstadoPaciente estado = Paciente.EstadoPaciente.valueOf(nuevoEstado);
            if (estado != Paciente.EstadoPaciente.ACTIVO && estado != Paciente.EstadoPaciente.INACTIVO) {
                redirectAttributes.addFlashAttribute("mensajeError", "Ese estado no se puede asignar desde este módulo.");
                return "redirect:/pacientes";
            }
            pacienteService.cambiarEstado(idPaciente, estado);
            redirectAttributes.addFlashAttribute("mensajeSuccess", "Estado del paciente actualizado correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Estado inválido.");
        }
        return "redirect:/pacientes";
    }

    /**
     * Exporta el directorio de pacientes (o el resultado de la búsqueda actual) a un archivo CSV,
     * compatible con Excel (incluye BOM UTF-8 para que tildes y Ñ se vean bien).
     */
    @GetMapping("/exportar-csv")
    public void exportarCsv(@RequestParam(value = "busqueda", required = false) String busqueda,
                             HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"pacientes.csv\"");

        List<Paciente> pacientes = pacienteService.buscarPacientes(busqueda);

        PrintWriter writer = response.getWriter();
        writer.write('\uFEFF'); // BOM: para que Excel detecte UTF-8 y muestre bien tildes/Ñ
        writer.println("Historia Clinica,DNI,Nombres,Apellidos,Fecha Nacimiento,Edad,Sexo,Telefono,Direccion,Correo,Alergias,Estado");

        for (Paciente p : pacientes) {
            writer.println(String.join(",",
                    csv(p.getNumeroHistoriaClinica()),
                    csv(p.getDni()),
                    csv(p.getNombres()),
                    csv(p.getApellidos()),
                    csv(p.getFechaNacimiento() != null ? p.getFechaNacimiento().toString() : ""),
                    csv(p.getEdad() != null ? p.getEdad().toString() : ""),
                    csv(p.getSexo() != null ? p.getSexo().name() : ""),
                    csv(p.getTelefono()),
                    csv(p.getDireccion()),
                    csv(p.getCorreo()),
                    csv(p.getAlergias()),
                    csv(p.getEstado() != null ? p.getEstado().name() : "")
            ));
        }
        writer.flush();
    }

    /**
     * Escapa un valor para CSV: si contiene coma, comillas o salto de línea, lo envuelve en comillas dobles.
     */
    private String csv(String valor) {
        if (valor == null) return "";
        String limpio = valor.replace("\"", "\"\"");
        if (limpio.contains(",") || limpio.contains("\"") || limpio.contains("\n")) {
            return "\"" + limpio + "\"";
        }
        return limpio;
    }

    /**
     * Muestra una vista imprimible del directorio de pacientes (para exportar a PDF con "Guardar como PDF" del navegador).
     */
    @GetMapping("/exportar-pdf")
    public String exportarPdf(@RequestParam(value = "busqueda", required = false) String busqueda, Model model) {
        List<Paciente> pacientes = pacienteService.buscarPacientes(busqueda);
        model.addAttribute("pacientes", pacientes);
        model.addAttribute("fechaGeneracion", LocalDateTime.now());
        model.addAttribute("totalPacientes", pacientes.size());
        return "pacientes-pdf";
    }
=======
        pacienteService.guardar(paciente);
        
        return "redirect:/pacientes";
    }
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a
}
