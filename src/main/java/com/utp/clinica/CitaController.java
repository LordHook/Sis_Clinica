package com.utp.clinica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaRepository citaRepo;

    // Obtener todas las citas para la tabla del dashboard
    @GetMapping
    public List<Cita> listarCitas() {
        return citaRepo.findAll();
    }

    // Agendar una nueva cita
    @PostMapping("/agendar")
    public ResponseEntity<?> agendarCita(@RequestBody Cita nuevaCita) {
        // Aquí podrías agregar validación de disponibilidad usando citaRepo.findByMedicoAndFechaHora(...)
        Cita guardada = citaRepo.save(nuevaCita);
        return ResponseEntity.ok(guardada);
    }
}