package com.utp.clinica.repository;

import com.utp.clinica.model.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio JPA para realizar consultas sobre la entidad Receta
 */
@Repository
public interface RecetaRepository extends JpaRepository<Receta, Integer> {

    /**
     * Obtiene recetas según su estado de despacho (EMITIDA, DESPACHADA)
     */
    List<Receta> findByEstado(Receta.EstadoReceta estado);
}
