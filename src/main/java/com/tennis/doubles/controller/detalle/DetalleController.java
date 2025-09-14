package com.tennis.doubles.controller.detalle;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tennis.doubles.dto.detalle.DetalleParejaDTO;
import com.tennis.doubles.service.detalle.DetalleService;

@Controller
@RequestMapping("/detalle")
public class DetalleController {

    private final DetalleService detalleService;

    public DetalleController(DetalleService detalleService) {
        this.detalleService = detalleService;
    }

    @GetMapping("/pareja/{id}")
    public String detallePareja(
    		@PathVariable("id") Long parejaId,
            @RequestParam(name = "superficie", required = false) String superficie,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(name = "categoria") String categoria,  // solo para volver al ranking
            @RequestParam(name = "minPartidos") int minPartidos, 
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
        List<DetalleParejaDTO> partidos = detalleService.obtenerDetallePareja(parejaId, fechaInicio, fechaFin, superficieBusqueda);
        
        model.addAttribute("partidos", partidos);
        StringBuilder url = new StringBuilder("/ranking/parejas?categoria=" + categoria);
        if (superficie != null) url.append("&superficie=").append(superficie);
        if (fechaInicio != null) url.append("&desde=").append(fechaInicio);
        if (fechaFin != null) url.append("&hasta=").append(fechaFin);
        url.append("&minPartidos=").append(minPartidos);
        if (favorito != null) url.append("&favorito=").append(favorito);
        model.addAttribute("urlRetorno", url.toString());
        model.addAttribute("parejaId", parejaId);
        model.addAttribute("titulo", "Últimos partidos - " + partidos.get(0).getPareja());

        return "detalle-pareja";
    }
}
