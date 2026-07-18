package com.utp.clinica.controller;

import com.utp.clinica.model.Medicamento;
import com.utp.clinica.service.FarmaciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@Controller
@RequestMapping("/inventario")
public class InventarioController {

    @Autowired
    private FarmaciaService farmaciaService;

    @PostMapping("/guardar")
    public String guardar(@RequestParam(value = "idMedicamento", required = false) Integer idMedicamento,
                          @RequestParam("nombre") String nombre,
                          @RequestParam("precio") BigDecimal precio,
                          @RequestParam("stock") Integer stock) {
        
        Medicamento medicamento = new Medicamento();
        if (idMedicamento != null) {
            medicamento.setIdMedicamento(idMedicamento);
        }
        medicamento.setNombre(nombre);
        medicamento.setPrecio(precio);
        medicamento.setStock(stock);
        
        farmaciaService.guardarMedicamento(medicamento);
        return "redirect:/inventario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Integer id) {
        try {
            farmaciaService.eliminarMedicamento(id);
            return "redirect:/inventario";
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return "redirect:/inventario?errorInUse=true";
        }
    }
}
