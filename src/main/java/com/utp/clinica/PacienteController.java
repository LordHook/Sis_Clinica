package com.utp.clinica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    @Autowired
    private PacienteRepository pacienteRepo;

    @GetMapping
    public List<Paciente> listarPacientes() {
        return pacienteRepo.findAll();
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarPaciente(@RequestBody Paciente paciente) {
        // Asegurar que el estado inicial sea ACTIVO
        paciente.setEstado("ACTIVO");
        
        // Autogenerar Historia Clínica (Ej: HC-000005)
        if (paciente.getNumeroHistoriaClinica() == null || paciente.getNumeroHistoriaClinica().isEmpty()) {
            long total = pacienteRepo.count() + 1;
            paciente.setNumeroHistoriaClinica(String.format("HC-%06d", total));
        }

        Paciente guardado = pacienteRepo.save(paciente);
        return ResponseEntity.ok(guardado);
    }
}