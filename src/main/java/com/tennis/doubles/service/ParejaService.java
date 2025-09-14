package com.tennis.doubles.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tennis.doubles.dto.rankings.RankingParejaDTO;
import com.tennis.doubles.model.Pareja;
import com.tennis.doubles.repository.ParejaRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ParejaService {
	
	private final ParejaRepository parejaRepository;
	
	public ParejaService(ParejaRepository parejaRepository) {
        this.parejaRepository = parejaRepository;
    }
	
	public void toggleFavorito(Long id) {
	    Pareja pareja = parejaRepository.findById(id)
	            .orElseThrow(() -> new EntityNotFoundException("Pareja no encontrada"));
	    pareja.setFavorito(!pareja.isFavorito());
	    parejaRepository.save(pareja);
	}

	public List<RankingParejaDTO> obtenerRankingParejas(String categoria, String superficie, LocalDate fechaInicio,
			LocalDate fechaFin, int minPartidos, Boolean favorito) {
		if (categoria.contains("ITF")) {
			return parejaRepository.obtenerRankingParejasITF(categoria, superficie, fechaInicio, fechaFin, minPartidos, favorito);
		}
		return parejaRepository.obtenerRankingParejas(categoria, superficie, fechaInicio, fechaFin, minPartidos, favorito);
	}

}
