package com.tennis.doubles.controller.rankings;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tennis.doubles.dto.rankings.RankingJugadorDTO;
import com.tennis.doubles.dto.rankings.RankingParejaDTO;
import com.tennis.doubles.service.rankings.RankingService;

@Controller
@RequestMapping("/ranking")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/jugadores")
    public String rankingJugadores(
            @RequestParam(name = "categoria", defaultValue = "ATP") String categoria,
            @RequestParam(name = "superficie", required = false) String superficie,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(name = "minPartidos", defaultValue = "20") int minPartidos,
            Model model
    ) {
    	String superficieBusqueda = null;
    	if (superficie != null && !superficie.isEmpty()) {
    		switch (superficie) {
    		case "Tierra":
    			superficieBusqueda = "Clay";
    			break;
    		case "Dura":
    			superficieBusqueda = "Hard";
    			break;
			default:
				superficieBusqueda = "Grass";
    		}
    	}
        List<RankingJugadorDTO> ranking = rankingService.obtenerRankingJugadores(
                categoria, superficieBusqueda, fechaInicio, fechaFin, minPartidos
        );

        model.addAttribute("ranking", ranking);
        model.addAttribute("categoria", categoria);
        model.addAttribute("superficie", superficie);
        model.addAttribute("desde", fechaInicio);
        model.addAttribute("hasta", fechaFin);
        model.addAttribute("minPartidos", minPartidos);

        return "ranking-jugadores";
    }
    
    @GetMapping("/parejas")
    public String rankingParejas(
            @RequestParam(name = "categoria", defaultValue = "ATP") String categoria,
            @RequestParam(name = "superficie", required = false) String superficie,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(name = "minPartidos", defaultValue = "20") int minPartidos,
            @RequestParam(name = "favorito", required = false) Boolean favorito,
            Model model
    ) {
    	String superficieBusqueda = null;
    	if (superficie != null && !superficie.isEmpty()) {
    		switch (superficie) {
    		case "Tierra":
    			superficieBusqueda = "Clay";
    			break;
    		case "Dura":
    			superficieBusqueda = "Hard";
    			break;
			default:
				superficieBusqueda = "Grass";
    		}
    	}
    	if (favorito == null) {
    		favorito = false;
    	}
        List<RankingParejaDTO> ranking = rankingService.obtenerRankingParejas(
                categoria, superficieBusqueda, fechaInicio, fechaFin, minPartidos, favorito
        );

        model.addAttribute("ranking", ranking);
        model.addAttribute("categoria", categoria);
        model.addAttribute("superficie", superficie);
        model.addAttribute("desde", fechaInicio);
        model.addAttribute("hasta", fechaFin);
        model.addAttribute("minPartidos", minPartidos);
        model.addAttribute("favorito", favorito);

        return "ranking-parejas";
    }
}
