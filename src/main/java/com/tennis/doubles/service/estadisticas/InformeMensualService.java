package com.tennis.doubles.service.estadisticas;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tennis.doubles.dto.estadisticas.RangoCuotaDTO;
import com.tennis.doubles.dto.estadisticas.ResumenMensualDTO;
import com.tennis.doubles.repository.PrediccionPartidoRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Service
public class InformeMensualService {

    @Autowired
    private PrediccionPartidoRepository prediccionPartidoRepository;

    public List<String> obtenerMesesConPronosticos() {
        return prediccionPartidoRepository.findMesesConPronosticos();
    }

    public ResumenMensualDTO obtenerResumenMensual(int anio, int mes) {
        YearMonth ym = YearMonth.of(anio, mes);
        LocalDate inicio = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();

        int total = prediccionPartidoRepository.countPronosticos(inicio, fin);
        int aciertos = prediccionPartidoRepository.countAciertos(inicio, fin);
        int fallos = prediccionPartidoRepository.countFallos(inicio, fin);

        double porcentaje = (total > 0) ? (aciertos * 100.0 / total) : 0;

        return new ResumenMensualDTO(total, aciertos, fallos, porcentaje);
    }
    
    public List<RangoCuotaDTO> obtenerAnalisisPorRangoCuota(int anio, int mes) {

        List<RangoCuotaDTO> resultado = new ArrayList<>();

        // Definir los rangos de cuota
        double[][] rangos = {
            {1.00, 1.10},
            {1.10, 1.20},
            {1.20, 1.30},
            {1.30, 1.40},
            {1.40, 1.50},
            {1.50, Double.MAX_VALUE}
        };

        String[] nombres = {
            "1,00 - 1,10",
            "1,10 - 1,20",
            "1,20 - 1,30",
            "1,30 - 1,40",
            "1,40 - 1,50",
            "+1,50"
        };

        // Para cada rango
        for (int i = 0; i < rangos.length; i++) {
            long aciertos = prediccionPartidoRepository.countByCuotaBetweenAndFecha(anio, mes, rangos[i][0], rangos[i][1], true);
            long fallos = prediccionPartidoRepository.countByCuotaBetweenAndFecha(anio, mes, rangos[i][0], rangos[i][1], false);
            double porcentaje = (aciertos + fallos) > 0 ? (aciertos * 100.0) / (aciertos + fallos) : 0;
            resultado.add(new RangoCuotaDTO(nombres[i], aciertos, fallos, porcentaje));
        }

        // Rango sin cuota
        long aciertos = prediccionPartidoRepository.countByCuotaIsNullAndFecha(anio, mes, true);
        long fallos = prediccionPartidoRepository.countByCuotaIsNullAndFecha(anio, mes, false);
        double porcentaje = (aciertos + fallos) > 0 ? (aciertos * 100.0) / (aciertos + fallos) : 0;
        resultado.add(new RangoCuotaDTO("Sin cuota", aciertos, fallos, porcentaje));

        return resultado;
    }
    
    public Map<String, Map<String, Long>> obtenerResumenPorRonda(int anio, int mes) {
        Map<String, Map<String, Long>> resumen = new LinkedHashMap<>();
        List<String> rondas = prediccionPartidoRepository.findRondasDisponibles(anio, mes);

        for (String ronda : rondas) {
            long aciertos = prediccionPartidoRepository.countByRondaAndFecha(anio, mes, ronda, true);
            long fallos = prediccionPartidoRepository.countByRondaAndFecha(anio, mes, ronda, false);
            Map<String, Long> datos = new LinkedHashMap<>();
            datos.put("aciertos", aciertos);
            datos.put("fallos", fallos);
            datos.put("total", aciertos + fallos);
            resumen.put(ronda, datos);
        }

        return resumen;
    }
    
    /**
     * Bloque 1: % de aciertos total por mes
     */
    public List<Map<String, Object>> obtenerEvolucionAciertoTotal() {
    	List<Object[]> totalRows = prediccionPartidoRepository.obtenerAciertosTotalesPorMes();
        List<Object[]> masculinoRows = prediccionPartidoRepository.obtenerAciertosMasculinoPorMes();
        List<Object[]> femeninoRows = prediccionPartidoRepository.obtenerAciertosFemeninoPorMes();
        List<Object[]> itfRows = prediccionPartidoRepository.obtenerAciertosITFPorMes();
        List<Object[]> challengerRows = prediccionPartidoRepository.obtenerAciertosChallengerPorMes();
        List<Object[]> atpWtaRows = prediccionPartidoRepository.obtenerAciertosATPWTAPorMes();

        Map<String, Map<String, Object>> resultado = new LinkedHashMap<>();

        // --- Función auxiliar para insertar datos de cualquier grupo ---
        BiConsumer<List<Object[]>, String> procesarGrupo = (rows, tipo) -> {
            for (Object[] r : rows) {
                String mes = (String) r[0];
                int aciertos = ((Number) r[1]).intValue();
                int total = ((Number) r[2]).intValue();
                double porcentaje = total == 0 ? 0.0 : (aciertos * 100.0 / total);

                Map<String, Object> m = resultado.computeIfAbsent(mes, k -> {
                    Map<String, Object> nuevo = new HashMap<>();
                    nuevo.put("mes", mes);
                    return nuevo;
                });
                m.put(tipo, porcentaje);
            }
        };

        // --- Procesar cada conjunto de datos ---
        procesarGrupo.accept(totalRows, "total");
        procesarGrupo.accept(masculinoRows, "masculino");
        procesarGrupo.accept(femeninoRows, "femenino");
        procesarGrupo.accept(itfRows, "itf");
        procesarGrupo.accept(challengerRows, "challenger");
        procesarGrupo.accept(atpWtaRows, "atp_wta");

        // --- Rellenar con 0 los valores faltantes (por consistencia del gráfico) ---
        for (Map<String, Object> m : resultado.values()) {
            m.putIfAbsent("total", 0.0);
            m.putIfAbsent("masculino", 0.0);
            m.putIfAbsent("femenino", 0.0);
            m.putIfAbsent("itf", 0.0);
            m.putIfAbsent("challenger", 0.0);
            m.putIfAbsent("atp_wta", 0.0);
        }

        return new ArrayList<>(resultado.values());
    }

    /**
     * Bloque 2: fallos por rango de cuota por mes
     */
    public Map<String, List<Map<String, Object>>> obtenerEvolucionAciertosPorCuota() {
        Map<String, List<Map<String, Object>>> map = new LinkedHashMap<>();
        String[] rangos = {"1-1.10","1.10-1.20","1.20-1.30","1.30-1.40","1.40-1.50",">1.50","sin cuota"};
        for (String rango : rangos) {
            List<Object[]> rows = prediccionPartidoRepository.obtenerAciertosPorCuotaRango(rango);
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Object[] r : rows) {
                Map<String, Object> m = new HashMap<>();
                m.put("mes", r[0]);      // "YYYY-MM"
                m.put("aciertos", ((Number) r[1]).intValue());
                double porcentaje = ((Number) r[2]).intValue() == 0 ? 0.0 :
                    ((Number) r[1]).doubleValue() * 100 / ((Number) r[2]).doubleValue();
                m.put("porcentaje", porcentaje);
                lista.add(m);
            }
            map.put(rango, lista);
        }
        return map;
    }

    /**
     * Bloque 3: fallos por ronda por mes
     */
    public Map<String, List<Map<String, Object>>> obtenerEvolucionAciertosPorRonda() {
        Map<String, List<Map<String, Object>>> map = new LinkedHashMap<>();
        List<String> rondas = prediccionPartidoRepository.obtenerRondasDistintas();
        for (String ronda : rondas) {
            List<Object[]> rows = prediccionPartidoRepository.obtenerAciertosPorRonda(ronda);
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Object[] r : rows) {
                Map<String, Object> m = new HashMap<>();
                m.put("mes", r[0]);   // "YYYY-MM"
                m.put("fallos", ((Number) r[1]).intValue());
                double porcentaje = ((Number) r[2]).intValue() == 0 ? 0.0 :
                    ((Number) r[1]).doubleValue() * 100 / ((Number) r[2]).doubleValue();
                m.put("porcentaje", porcentaje);
                lista.add(m);
            }
            map.put(ronda, lista);
        }
        return map;
    }
}
