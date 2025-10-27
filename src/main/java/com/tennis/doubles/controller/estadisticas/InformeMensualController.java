package com.tennis.doubles.controller.estadisticas;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tennis.doubles.dto.estadisticas.MesDisponible;
import com.tennis.doubles.dto.estadisticas.ResumenMensualDTO;
import com.tennis.doubles.service.estadisticas.InformeMensualService;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class InformeMensualController {

    @Autowired
    private InformeMensualService informeMensualService;

    @GetMapping("/informe/mensual")
    public String mostrarInformeMensual(@RequestParam(name = "anio", required = false) Integer anio,
                                        @RequestParam(name = "mes", required = false) Integer mes,
                                        Model model) {

    	// mesesDisponibles viene como List<String> formato "YYYY-MM" desde el repo/service
        List<String> mesesRaw = informeMensualService.obtenerMesesConPronosticos();

        // Convertir a objetos MesDisponible con nombre del mes en español
        Locale localeEs = new Locale("es", "ES");
        List<MesDisponible> mesesDisponibles = mesesRaw.stream()
                // Convertir a MesDisponible
                .map(s -> {
                    int y = Integer.parseInt(s.substring(0, 4));
                    int m = Integer.parseInt(s.substring(5, 7));
                    String nombreMes = Month.of(m).getDisplayName(TextStyle.FULL, localeEs);
                    nombreMes = Character.toUpperCase(nombreMes.charAt(0)) + nombreMes.substring(1);
                    return new MesDisponible(y, m, nombreMes, s);
                })
                // Eliminar duplicados por mes
                .collect(Collectors.collectingAndThen(
                    Collectors.toMap(MesDisponible::getMes, Function.identity(), (existing, replacement) -> existing),
                    map -> map.values().stream()
                              .sorted(Comparator.comparingInt(MesDisponible::getMes)) // ordenar de enero a diciembre
                              .toList()
                ));

        // Lista de años únicos (ordenados desc)
        List<Integer> aniosDisponibles = mesesDisponibles.stream()
                .map(MesDisponible::getAnio)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();

        model.addAttribute("mesesDisponibles", mesesDisponibles);
        model.addAttribute("aniosDisponibles", aniosDisponibles);

        // Si se seleccionó mes+anio, calcular resumen y título legible
        if (anio != null && mes != null) {
            ResumenMensualDTO resumen = informeMensualService.obtenerResumenMensual(anio, mes);
            model.addAttribute("resumen", resumen);
            model.addAttribute("anio", anio);
            model.addAttribute("mes", mes);

            String nombreMes = java.time.Month.of(mes)
                    .getDisplayName(java.time.format.TextStyle.FULL, localeEs);
            String titulo = Character.toUpperCase(nombreMes.charAt(0)) + nombreMes.substring(1) + " de " + anio;
            model.addAttribute("tituloMes", titulo);
            
            model.addAttribute("analisisCuotas", informeMensualService.obtenerAnalisisPorRangoCuota(anio, mes));
            
            Map<String, Map<String, Long>> resumenRondas = informeMensualService.obtenerResumenPorRonda(anio, mes);
            model.addAttribute("resumenRondas", resumenRondas);
        }

        return "informe-mensual";
    }
    
    @GetMapping("/informe/evolucion")
    public String mostrarGraficosEvolucion(Model model) {

        // Bloque 1: % de aciertos total, itf/atp, masculino/femenino por mes
        List<Map<String, Object>> evolucionAciertoTotal = informeMensualService.obtenerEvolucionAciertoTotal();
        model.addAttribute("evolucionAciertoTotal", evolucionAciertoTotal);

        // Bloque 2: fallos por rango de cuota por mes
        Map<String, List<Map<String, Object>>> evolucionAciertosPorCuota = informeMensualService.obtenerEvolucionAciertosPorCuota();
        model.addAttribute("evolucionAciertosPorCuota", evolucionAciertosPorCuota);

        // Bloque 3: fallos por ronda por mes
        Map<String, List<Map<String, Object>>> evolucionAciertosPorRonda = informeMensualService.obtenerEvolucionAciertosPorRonda();
        model.addAttribute("evolucionAciertosPorRonda", evolucionAciertosPorRonda);

        return "informe-evolucion"; // nombre del html
    }
}
