package com.tennis.doubles.controller.estadisticas;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tennis.doubles.dto.estadisticas.EstadisticasPronosticosDTO;
import com.tennis.doubles.dto.estadisticas.PronosticoPartidoDTO;
import com.tennis.doubles.service.PrediccionPartidoService;
import com.tennis.doubles.utils.Constantes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PrediccionController {

    private final PrediccionPartidoService prediccionService;

    @GetMapping("/predicciones")
    public String verEstadisticasPredicciones(
            @RequestParam(name = "fechaDesde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(name = "fechaHasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            Model model) {

        if (fechaDesde == null) fechaDesde = LocalDate.now().minusDays(1);
        if (fechaHasta == null) fechaHasta = LocalDate.now().minusDays(1);

        List<PronosticoPartidoDTO> listaPredicciones =
                prediccionService.obtenerPredicciones(fechaDesde, fechaHasta);

        EstadisticasPronosticosDTO total =
                prediccionService.calcularEstadisticas(listaPredicciones);
        EstadisticasPronosticosDTO cuotaAlta = prediccionService.calcularEstadisticasPorFiltro(listaPredicciones, p -> p.getCuota() != null && p.getCuota() >= 1.20);
        EstadisticasPronosticosDTO masculino = prediccionService.calcularEstadisticasPorFiltro(listaPredicciones, p -> !p.isFemenino());
        EstadisticasPronosticosDTO femenino = prediccionService.calcularEstadisticasPorFiltro(listaPredicciones, p -> p.isFemenino());
        EstadisticasPronosticosDTO atpWta = prediccionService.calcularEstadisticasPorFiltro(listaPredicciones, p -> !p.getCategoria().contains(Constantes.ITF));
        EstadisticasPronosticosDTO itf = prediccionService.calcularEstadisticasPorFiltro(listaPredicciones, p -> p.getCategoria().contains(Constantes.ITF));

        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("partidos", listaPredicciones);
        model.addAttribute("estadisticas", total);
        model.addAttribute("estadisticasMasculino", masculino);
        model.addAttribute("estadisticasFemenino", femenino);
        model.addAttribute("estadisticasATP_WTA", atpWta);
        model.addAttribute("estadisticasITF", itf);
        model.addAttribute("estadisticasCuotaAlta", cuotaAlta);

        return "estadisticas-pronosticos";
    }
}
