package com.utp.clinica.repository;

import com.utp.clinica.model.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para realizar consultas sobre la entidad Medicamento
 */
@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Integer> {
}
