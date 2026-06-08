package com.utp.clinica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/farmacia")
public class FarmaciaController {

    @Autowired 
    private RecetaRepository recetaRepo;
    
    @Autowired 
    private MedicamentoRepository medRepo;

    /**
     * Lista todas las recetas que están pendientes de entrega (estado 'EMITIDA')
     */
    @GetMapping("/pendientes")
    public List<Receta> listarPendientes() {
        return recetaRepo.findByEstado("EMITIDA");
    }

    /**
     * Procesa el despacho de una receta específica
     */
    @PostMapping("/despachar/{idReceta}")
    public ResponseEntity<String> despacharReceta(@PathVariable Long idReceta) {
        Optional<Receta> recetaOpt = recetaRepo.findById(idReceta);
        
        if (recetaOpt.isEmpty()) {
            return ResponseEntity.status(404).body("RECETA_NO_ENCONTRADA");
        }

        Receta receta = recetaOpt.get();
        Medicamento med = receta.getMedicamento();

        
        // Validación de stock
        if (med.getStock() >= receta.getCantidad()) {
            // Actualizar inventario
            med.setStock(med.getStock() - receta.getCantidad());
            // Actualizar estado de la receta
            receta.setEstado("DESPACHADA");
            
            // Persistir cambios en la base de datos
            medRepo.save(med);
            recetaRepo.save(receta);
            
            return ResponseEntity.ok("DESPACHO_EXITOSO");
        }
        
        return ResponseEntity.status(400).body("STOCK_INSUFICIENTE");
    }
    @GetMapping("/listar")
public List<Receta> listarRecetas(@RequestParam(required = false) String estado, 
                                  @RequestParam(required = false) String busqueda) {
    // Si no envías nada, trae todas. Si envías estado, filtra.
    if (estado != null && !estado.isEmpty()) {
        return recetaRepo.findByEstado(estado);
    }
    return recetaRepo.findAll();
}
}