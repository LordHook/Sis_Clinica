package com.utp.clinica.repository;

import com.utp.clinica.model.Paciente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio JPA para realizar consultas sobre la entidad Paciente
 */
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {

    /**
     * Realiza una búsqueda flexible de pacientes basada en su DNI, Historia clínica, nombres o apellidos
     */
    List<Paciente> findByDniContainingOrNumeroHistoriaClinicaContainingOrNombresContainingOrApellidosContaining(
            String dni, String numeroHistoriaClinica, String nombres, String apellidos);

    /**
     * Misma búsqueda flexible, pero paginada y ordenable (para el directorio de pacientes)
     */
    Page<Paciente> findByDniContainingOrNumeroHistoriaClinicaContainingOrNombresContainingOrApellidosContaining(
            String dni, String numeroHistoriaClinica, String nombres, String apellidos, Pageable pageable);

    /**
     * Verifica si ya existe un paciente con ese DNI (para registro nuevo)
     */
    boolean existsByDni(String dni);

    /**
     * Verifica si existe OTRO paciente (distinto del que se está editando) con ese DNI
     */
    boolean existsByDniAndIdPacienteNot(String dni, Integer idPaciente);
}
