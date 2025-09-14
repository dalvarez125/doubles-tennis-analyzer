package com.tennis.doubles.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tennis.doubles.model.Torneo;
import com.tennis.doubles.repository.TorneoRepository;

@Service
public class TorneoService {
	
	private final TorneoRepository torneoRepository;
	
	public TorneoService(TorneoRepository torneoRepository) {
        this.torneoRepository = torneoRepository;
    }

	Optional<Torneo> findByNombreAndAnio(String nombreTorneo, int anio) {
		return torneoRepository.findByNombreAndAnio(nombreTorneo, anio);
	}

}
