package com.tennis.doubles.repository;

import com.tennis.doubles.model.Torneo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TorneoRepository extends JpaRepository<Torneo, String> {
	
	Optional<Torneo> findByNombreAndAnio(String nombre, int anio);
}
