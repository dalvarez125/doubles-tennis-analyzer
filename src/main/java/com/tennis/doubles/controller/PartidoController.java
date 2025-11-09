package com.tennis.doubles.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tennis.doubles.dto.carga.partidos.ProximosPartidosDTO;
import com.tennis.doubles.service.PartidoService;

@Controller
@RequestMapping("/partido")
public class PartidoController {
	
	PartidoService partidoService;
	
	public PartidoController(PartidoService partidoService) {
		super();
		this.partidoService = partidoService;
	}

	@GetMapping("/proximos")
	public String mostrarPartidosFuturos(@RequestParam(name = "fecha", required = false, defaultValue = "hoy") String fecha,
	        							 @RequestParam(name = "categoria", required = false, defaultValue = "ATP") String categoria,
	                                     @RequestParam(name = "favorito", defaultValue = "false") boolean favorito,
	                                     @RequestParam(name = "ordenPronostico", defaultValue = "false") boolean ordenPronostico,
	                                     @RequestParam(name = "soloCerrados", defaultValue = "false") boolean soloCerrados,
	                                     @RequestParam(name = "refrescar", defaultValue = "false") boolean refrescar,
	                                     Model model) {
	    List<ProximosPartidosDTO> filtrados = partidoService.getProximosPartidos(categoria, favorito, ordenPronostico, soloCerrados, refrescar, fecha);
	 // Fechas de pestañas
	    LocalDate hoy = LocalDate.now();
	    LocalDate manana = hoy.plusDays(1);
	    LocalDate pasado = hoy.plusDays(2);
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	    model.addAttribute("partidos", filtrados);
	    model.addAttribute("fecha", fecha);
	    model.addAttribute("fechaHoy", hoy.format(formatter));
	    model.addAttribute("fechaManana", manana.format(formatter));
	    model.addAttribute("fechaPasado", pasado.format(formatter));
	    model.addAttribute("categoria", categoria);
	    model.addAttribute("favorito", favorito);
	    model.addAttribute("ordenPronostico", ordenPronostico);
	    model.addAttribute("soloCerrados", soloCerrados);
	    return "proximos-partidos";
	}
	
	@GetMapping("/detalle/{id}")
	public String detallePartido(@PathVariable(name="id") Long id, 
			@RequestParam(name = "fecha", required = false) String fecha,
			@RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "favorito", defaultValue = "false") Boolean favorito,
            @RequestParam(name = "ordenPronostico", defaultValue = "false") Boolean ordenPronostico,Model model) {
	    ProximosPartidosDTO partido = partidoService.obtenerPartidoConComparativa(id, fecha, categoria);
	    model.addAttribute("partido", partido);
	    model.addAttribute("titulo", "Detalle partido - " + partido.getParejaLocalNombre() + " VS. " + partido.getParejaVisitanteNombre());
	    
	    StringBuilder url = new StringBuilder("/partido/proximos");
        if (favorito != null) url.append("?favorito=").append(favorito);
        if (categoria != null) url.append("&categoria=").append(categoria);
        if (fecha != null) url.append("&fecha=").append(fecha);
        if (ordenPronostico != null) url.append("&ordenPronostico=").append(ordenPronostico);
        model.addAttribute("urlRetorno", url.toString());
	    return "detalle-partido";
	}
	
}

