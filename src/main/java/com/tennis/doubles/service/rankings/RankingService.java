package com.tennis.doubles.service.rankings;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tennis.doubles.dto.rankings.RankingJugadorDTO;
import com.tennis.doubles.dto.rankings.RankingParejaDTO;
import com.tennis.doubles.service.JugadorService;
import com.tennis.doubles.service.ParejaService;

@Service
public class RankingService {

    private final JugadorService jugadorService;
    private final ParejaService parejaService;

    public RankingService(JugadorService jugadorService, ParejaService parejaService) {
        this.jugadorService = jugadorService;
		this.parejaService = parejaService;
    }

    public List<RankingJugadorDTO> obtenerRankingJugadores(
    	    String categoria, 
    	    String superficie, 
    	    LocalDate fechaInicio, 
    	    LocalDate fechaFin, 
    	    int minPartidos) {
    	return jugadorService.obtenerRankingJugadores(categoria, superficie, fechaInicio, fechaFin, minPartidos);
    } 
    
    public List<RankingParejaDTO> obtenerRankingParejas(
    	    String categoria, 
    	    String superficie, 
    	    LocalDate fechaInicio, 
    	    LocalDate fechaFin, 
    	    int minPartidos, Boolean favorito) {
    	return parejaService.obtenerRankingParejas(categoria, superficie, fechaInicio, fechaFin, minPartidos, favorito);
    }
}
