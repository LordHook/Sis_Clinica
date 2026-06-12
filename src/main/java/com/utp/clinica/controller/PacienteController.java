package com.utp.clinica.controller;

import com.utp.clinica.model.Paciente;
import com.utp.clinica.service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/**
 * Controlador encargado de procesar las peticiones y altas de pacientes
 */
@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

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
                          @RequestParam(value = "alergias", required = false) String alergias) {
        
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

        pacienteService.guardar(paciente);
        
        return "redirect:/pacientes";
    }
}
