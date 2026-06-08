package com.utp.clinica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired 
    private CitaRepository citaRepo; 

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        // Si CitaRepository existe, esto funcionará
        stats.put("totalCitas", citaRepo.count()); 
        stats.put("citasPorEspecialidad", Arrays.asList(18, 12, 7, 5));
        return stats;
    }
}