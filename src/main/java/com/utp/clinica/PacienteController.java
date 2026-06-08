package com.utp.clinica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    @Autowired
    private PacienteRepository pacienteRepository;

    // Obtener todos los pacientes
    @GetMapping
    public List<Paciente> listarPacientes() {
        return pacienteRepository.findAll();
    }

    // Guardar un nuevo paciente
    @PostMapping
    public ResponseEntity<Paciente> guardarPaciente(@RequestBody Paciente paciente) {
        // Generar un número de historia clínica automático
        paciente.setNumero_historia_clinica("HC-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        paciente.setEstado("ACTIVO");
        
        Paciente nuevoPaciente = pacienteRepository.save(paciente);
        return ResponseEntity.ok(nuevoPaciente);
    }
}