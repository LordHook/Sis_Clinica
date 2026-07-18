package com.utp.clinica.service;

import com.utp.clinica.model.Paciente;
import com.utp.clinica.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
<<<<<<< HEAD
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
=======
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Servicio encargado de gestionar los expedientes e historial clínico de pacientes
 */
@Service
public class PacienteService {

    @Autowired
    private PacienteRepository pacienteRepo;

    /**
     * Devuelve todos los pacientes del directorio
     */
    public List<Paciente> listarTodos() {
        return pacienteRepo.findAll();
    }

    /**
     * Busca un paciente por su DNI o Historia clínica
     */
    public List<Paciente> buscarPacientes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return pacienteRepo.findAll();
        }
        return pacienteRepo.findByDniContainingOrNumeroHistoriaClinicaContainingOrNombresContainingOrApellidosContaining(
                query, query, query, query);
    }

    /**
     * Busca un paciente individual por su ID primario
     */
    public Optional<Paciente> buscarPorId(Integer id) {
        return pacienteRepo.findById(id);
    }

    /**
<<<<<<< HEAD
     * Igual que buscarPacientes, pero devuelve una página (para el directorio con paginación y orden)
     */
    public Page<Paciente> buscarPacientesPaginado(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return pacienteRepo.findAll(pageable);
        }
        return pacienteRepo.findByDniContainingOrNumeroHistoriaClinicaContainingOrNombresContainingOrApellidosContaining(
                query, query, query, query, pageable);
    }

    /**
     * Verifica si el DNI ya está en uso por otro paciente (para validación en vivo desde el formulario)
     */
    public boolean existeDni(String dni, Integer idPacienteExcluir) {
        if (idPacienteExcluir == null) {
            return pacienteRepo.existsByDni(dni);
        }
        return pacienteRepo.existsByDniAndIdPacienteNot(dni, idPacienteExcluir);
    }

    /**
=======
>>>>>>> 04de8fab4a00084a57d92da688cda143f373db7a
     * Registra o actualiza la ficha del paciente. Autogenera el número de HC si es nuevo.
     */
    @Transactional
    public Paciente guardar(Paciente paciente) {
        if (paciente.getIdPaciente() == null) {
            paciente.setEstado(Paciente.EstadoPaciente.ACTIVO);
            // Autogeneración del código de Historia Clínica: HC-000001 en base al conteo
            long count = pacienteRepo.count() + 1;
            paciente.setNumeroHistoriaClinica(String.format("HC-%06d", count));
        } else {
            // Caso edición: mantener campos no editables si no vienen en la petición
            Paciente pacienteExistente = pacienteRepo.findById(paciente.getIdPaciente()).orElse(null);
            if (pacienteExistente != null) {
                paciente.setNumeroHistoriaClinica(pacienteExistente.getNumeroHistoriaClinica());
                paciente.setFechaRegistro(pacienteExistente.getFechaRegistro());
                if (paciente.getEstado() == null) {
                    paciente.setEstado(pacienteExistente.getEstado());
                }
            }
        }
        return pacienteRepo.save(paciente);
    }

    /**
     * Cambia el estado del paciente (ACTIVO, INACTIVO, FALLECIDO)
     */
    @Transactional
    public void cambiarEstado(Integer idPaciente, Paciente.EstadoPaciente estado) {
        pacienteRepo.findById(idPaciente).ifPresent(p -> {
            p.setEstado(estado);
            pacienteRepo.save(p);
        });
    }
}
