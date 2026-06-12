package com.utp.clinica.service;

import com.utp.clinica.model.Medicamento;
import com.utp.clinica.model.Receta;
import com.utp.clinica.repository.MedicamentoRepository;
import com.utp.clinica.repository.RecetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio encargado de gestionar el stock de medicamentos y el despacho de recetas médicas
 */
@Service
public class FarmaciaService {

    @Autowired
    private RecetaRepository recetaRepo;

    @Autowired
    private MedicamentoRepository medicamentoRepo;

    /**
     * Lista todas las recetas registradas en el sistema
     */
    public List<Receta> listarTodas() {
        return recetaRepo.findAll();
    }

    /**
     * Filtra recetas por su estado (EMITIDA, DESPACHADA)
     */
    public List<Receta> listarPorEstado(Receta.EstadoReceta estado) {
        return recetaRepo.findByEstado(estado);
    }

    /**
     * Filtra recetas por estado y DNI del paciente para la cola de búsqueda
     */
    public List<Receta> buscarRecetas(String query, Receta.EstadoReceta estado) {
        List<Receta> todas = (estado != null) ? recetaRepo.findByEstado(estado) : recetaRepo.findAll();
        if (query == null || query.trim().isEmpty()) {
            return todas;
        }
        String q = query.toLowerCase();
        return todas.stream()
                .filter(r -> r.getConsulta().getCita().getPaciente().getDni().contains(q)
                        || r.getConsulta().getCita().getPaciente().getNombres().toLowerCase().contains(q)
                        || r.getConsulta().getCita().getPaciente().getApellidos().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    /**
     * Procesa la entrega física de un medicamento a un paciente, reduciendo el stock disponible
     * 
     * @param idReceta ID de la receta emitida por el médico
     * @throws IllegalStateException Si el stock es insuficiente o la receta ya fue despachada
     */
    @Transactional
    public void despacharReceta(Integer idReceta) {
        Receta receta = recetaRepo.findById(idReceta)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la receta con ID: " + idReceta));

        if (receta.getEstado() == Receta.EstadoReceta.DESPACHADA) {
            throw new IllegalStateException("Esta receta ya fue despachada con anterioridad.");
        }

        Medicamento medicamento = receta.getMedicamento();
        if (medicamento.getStock() < receta.getCantidad()) {
            throw new IllegalStateException("Stock insuficiente para el medicamento: " + medicamento.getNombre() 
                                             + " (Disponibles: " + medicamento.getStock() + ", Requeridos: " + receta.getCantidad() + ")");
        }

        // Restamos el inventario y actualizamos los registros correspondientes
        medicamento.setStock(medicamento.getStock() - receta.getCantidad());
        receta.setEstado(Receta.EstadoReceta.DESPACHADA);

        medicamentoRepo.save(medicamento);
        recetaRepo.save(receta);
    }

    /**
     * Devuelve la lista completa de medicamentos disponibles
     */
    public List<Medicamento> listarMedicamentos() {
        return medicamentoRepo.findAll();
    }

    /**
     * Agrega stock a un medicamento
     */
    @Transactional
    public Medicamento reponerStock(Integer idMedicamento, int cantidad) {
        Medicamento med = medicamentoRepo.findById(idMedicamento)
                .orElseThrow(() -> new IllegalArgumentException("Medicamento no encontrado"));
        med.setStock(med.getStock() + cantidad);
        return medicamentoRepo.save(med);
    }
}
