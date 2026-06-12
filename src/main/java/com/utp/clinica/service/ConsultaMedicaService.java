package com.utp.clinica.service;

import com.utp.clinica.model.Cita;
import com.utp.clinica.model.ConsultaMedica;
import com.utp.clinica.model.Receta;
import com.utp.clinica.repository.CitaRepository;
import com.utp.clinica.repository.ConsultaMedicaRepository;
import com.utp.clinica.repository.RecetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Servicio encargado de gestionar el acto médico y el registro de la evolución del paciente
 */
@Service
public class ConsultaMedicaService {

    @Autowired
    private ConsultaMedicaRepository consultaRepo;

    @Autowired
    private CitaRepository citaRepo;

    @Autowired
    private RecetaRepository recetaRepo;

    /**
     * Busca el registro de una consulta por su ID primario
     */
    public Optional<ConsultaMedica> buscarPorId(Integer id) {
        return consultaRepo.findById(id);
    }

    /**
     * Busca la consulta médica de una cita específica
     */
    public Optional<ConsultaMedica> buscarPorCita(Integer idCita) {
        return consultaRepo.findByCitaIdCita(idCita);
    }

    /**
     * Registra el acto médico de una consulta y guarda sus recetas asociadas
     * Además, actualiza automáticamente el estado de la cita a 'ATENDIDA'
     * 
     * @param consulta Objeto con los datos de triaje y observaciones clínicas
     * @param recetas Lista de medicamentos recetados con dosis y cantidad
     * @return Registro de consulta guardado
     */
    @Transactional
    public ConsultaMedica registrarConsulta(ConsultaMedica consulta, List<Receta> recetas) {
        // Guardamos la consulta médica en primer lugar
        ConsultaMedica guardada = consultaRepo.save(consulta);

        // Actualizamos el estado de la cita médica relacionada a ATENDIDA
        Cita cita = consulta.getCita();
        cita.setEstado(Cita.EstadoCita.ATENDIDA);
        citaRepo.save(cita);

        // Si existen recetas especificadas, las vinculamos a la consulta y las guardamos
        if (recetas != null && !recetas.isEmpty()) {
            for (Receta receta : recetas) {
                receta.setConsulta(guardada);
                receta.setEstado(Receta.EstadoReceta.EMITIDA);
                recetaRepo.save(receta);
            }
        }

        return guardada;
    }
}
