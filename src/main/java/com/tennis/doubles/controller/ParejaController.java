package com.tennis.doubles.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import com.tennis.doubles.service.ParejaService;

@Controller
@RequestMapping("/pareja")
public class ParejaController {
	
	private final ParejaService parejaService;

    public ParejaController(ParejaService parejaService) {
        this.parejaService = parejaService;
    }

    @PostMapping("/favorito/{id}")
    public String toggleFavorito(@PathVariable("id") Long id,
                                 @RequestParam(name = "categoria") String categoria,
                                 @RequestParam(name = "superficie", required = false) String superficie,
                                 @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                 @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                 @RequestParam(name = "minPartidos", required = false) Integer minPartidos) {

        parejaService.toggleFavorito(id);

        // Redirigir manteniendo los filtros
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/ranking/parejas")
                .queryParam("categoria", categoria);
        if (superficie != null) uriBuilder.queryParam("superficie", superficie);
        if (desde != null) uriBuilder.queryParam("desde", desde);
        if (hasta != null) uriBuilder.queryParam("hasta", hasta);
        if (minPartidos != null) uriBuilder.queryParam("minPartidos", minPartidos);

        return "redirect:" + uriBuilder.toUriString();
    }
}

