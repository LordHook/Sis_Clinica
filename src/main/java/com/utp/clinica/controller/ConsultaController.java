package com.utp.clinica.controller;

import com.utp.clinica.model.*;
import com.utp.clinica.service.CitaService;
import com.utp.clinica.service.ConsultaMedicaService;
import com.utp.clinica.service.FarmaciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controlador encargado de procesar el registro de actos médicos y recetas
 */
@Controller
@RequestMapping("/api/consultas")
public class ConsultaController {

    @Autowired private ConsultaMedicaService consultaService;
    @Autowired private CitaService citaService;
    @Autowired private FarmaciaService farmaciaService;

    @Autowired private com.utp.clinica.repository.MedicamentoRepository medicamentoRepo;

    /**
     * Registra una nueva consulta y emite sus recetas asociadas
     */
    @PostMapping("/registrar")
    @ResponseBody
    public ResponseEntity<?> registrar(@RequestBody Map<String, Object> payload) {
        try {
            Integer idCita = Integer.valueOf(payload.get("idCita").toString());
            String presion = payload.get("presionArterial") != null ? payload.get("presionArterial").toString() : "";
            BigDecimal temp = payload.get("temperatura") != null ? new BigDecimal(payload.get("temperatura").toString()) : BigDecimal.ZERO;
            BigDecimal peso = payload.get("pesoKg") != null ? new BigDecimal(payload.get("pesoKg").toString()) : BigDecimal.ZERO;
            Integer talla = payload.get("tallaCm") != null ? Integer.valueOf(payload.get("tallaCm").toString()) : 0;
            Integer fCard = payload.get("frecuenciaCardiaca") != null ? Integer.valueOf(payload.get("frecuenciaCardiaca").toString()) : 0;
            String motivo = payload.get("motivoConsulta").toString();
            String diag = payload.get("diagnosticoCie10") != null ? payload.get("diagnosticoCie10").toString() : "";
            String evolucion = payload.get("evolucion") != null ? payload.get("evolucion").toString() : "";

            Cita cita = citaService.buscarPorId(idCita)
                    .orElseThrow(() -> new IllegalArgumentException("La cita no existe."));

            ConsultaMedica consulta = new ConsultaMedica();
            consulta.setCita(cita);
            consulta.setPresionArterial(presion);
            consulta.setTemperatura(temp);
            consulta.setPesoKg(peso);
            consulta.setTallaCm(talla);
            consulta.setFrecuenciaCardiaca(fCard);
            consulta.setMotivoConsulta(motivo);
            consulta.setDiagnosticoCie10(diag);
            consulta.setEvolucion(evolucion);

            // Procesar las recetas
            List<Receta> recetas = new ArrayList<>();
            if (payload.containsKey("recetas")) {
                List<Map<String, Object>> recetasList = (List<Map<String, Object>>) payload.get("recetas");
                for (Map<String, Object> rMap : recetasList) {
                    Integer idMedicamento = Integer.valueOf(rMap.get("idMedicamento").toString());
                    String dosis = rMap.get("dosis").toString();
                    String frecuencia = rMap.get("frecuencia").toString();
                    int cantidad = Integer.parseInt(rMap.get("cantidad").toString());

                    Medicamento med = medicamentoRepo.findById(idMedicamento)
                            .orElseThrow(() -> new IllegalArgumentException("Medicamento no encontrado"));

                    Receta receta = new Receta();
                    receta.setMedicamento(med);
                    receta.setDosis(dosis);
                    receta.setFrecuencia(frecuencia);
                    receta.setCantidad(cantidad);

                    recetas.add(receta);
                }
            }

            ConsultaMedica guardada = consultaService.registrarConsulta(consulta, recetas);
            return ResponseEntity.ok(Map.of("status", "SUCCESS", "idConsulta", guardada.getIdConsulta()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
