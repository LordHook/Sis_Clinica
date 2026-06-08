package com.utp.clinica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/citas/opciones")
public class ModalCitasController {

    @Autowired private EspecialidadRepository especialidadRepo;
    @Autowired private ConsultorioRepository consultorioRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private PacienteRepository pacienteRepo;

    @GetMapping
    public Map<String, Object> obtenerOpcionesModal() {
        Map<String, Object> opciones = new HashMap<>();
        
        opciones.put("especialidades", especialidadRepo.findAll());
        opciones.put("consultorios", consultorioRepo.findAll());
        opciones.put("pacientes", pacienteRepo.findAll()); // En un sistema enorme, esto sería una búsqueda dinámica, pero para iniciar está perfecto.
        
        // Asumiendo que en Usuario tienes un campo 'rol' y el enum correspondiente.
        // Si no tienes este método en el repositorio, créalo: List<Usuario> findByRol(String rol);
        // opciones.put("medicos", usuarioRepo.findByRol("MEDICO")); 
        
        // NOTA: Si aún no tienes usuarioRepo.findByRol, puedes mandar todos temporalmente:
        opciones.put("medicos", usuarioRepo.findAll()); 

        return opciones;
    }
}