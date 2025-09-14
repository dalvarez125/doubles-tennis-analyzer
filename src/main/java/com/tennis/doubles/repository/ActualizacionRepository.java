package com.tennis.doubles.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tennis.doubles.model.Actualizacion;

public interface ActualizacionRepository extends JpaRepository<Actualizacion, Long> {

    // Nos interesa el último registro insertado
    Actualizacion findTopByOrderByFechaUltimaActualizacionDesc();
}