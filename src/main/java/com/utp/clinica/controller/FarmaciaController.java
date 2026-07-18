package com.utp.clinica.controller;

import com.utp.clinica.service.FarmaciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Controlador REST encargado de gestionar el flujo de despacho en Farmacia
 */
@RestController
@RequestMapping("/api/farmacia")
public class FarmaciaController {

    @Autowired
    private FarmaciaService farmaciaService;

    /**
     * Confirma la entrega de una receta de medicamentos, disminuyendo el stock
     */
    @PostMapping("/despachar/{idReceta}")
    public ResponseEntity<?> despachar(@PathVariable("idReceta") Integer idReceta) {
        try {
            farmaciaService.despacharReceta(idReceta);
            return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "DESPACHO_EXITOSO"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }
}
