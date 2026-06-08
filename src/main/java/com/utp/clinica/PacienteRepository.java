package com.utp.clinica;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    // Spring Boot implementará automáticamente los métodos 
    // como .save(), .findAll(), .findById(), etc.
}