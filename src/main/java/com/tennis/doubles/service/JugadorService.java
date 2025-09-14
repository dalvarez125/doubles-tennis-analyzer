package com.tennis.doubles.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tennis.doubles.dto.rankings.RankingJugadorDTO;
import com.tennis.doubles.repository.JugadorRepository;

@Service
public class JugadorService {
	
	private final JugadorRepository jugadorRepository;
	
	public JugadorService(JugadorRepository jugadorRepository) {
        this.jugadorRepository = jugadorRepository;
    }

	public List<RankingJugadorDTO> obtenerRankingJugadores(String categoria, String superficie, LocalDate fechaInicio,
			LocalDate fechaFin, int minPartidos) {
		if (categoria.contains("ITF") ) {
			return jugadorRepository.obtenerRankingJugadoresITF(categoria, superficie, fechaInicio, fechaFin, minPartidos);
		}
		return jugadorRepository.obtenerRankingJugadores(categoria, superficie, fechaInicio, fechaFin, minPartidos);
	}

}
