package com.tennis.doubles;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "comparativa")
@Getter
@Setter
public class PesosComparativaConfig {

	private Map<String, Integer> atp;
    private Map<String, Integer> itf;
    private Map<String, Integer> challenger;

    public int getPeso(String categoria, String criterio, Integer partidos) {
        if (categoria == null || criterio == null) return 0;

        categoria = categoria.toLowerCase();
        criterio = criterio.toLowerCase();

        switch (categoria) {
            case "atp":
            case "wta":
            	if (partidos == null || partidos >= 20) {
            		return atp.getOrDefault(criterio, 0);
            	} else {
            		return atp.getOrDefault(criterio, 0)/2;
            	}
            case "itf men":
            case "itf women":
            case "itf":
            	if (partidos == null || partidos >= 20) {
            		return itf.getOrDefault(criterio, 0);
            	} else {
            		return itf.getOrDefault(criterio, 0)/2;
            	}
            case "challenger":
            case "wta 125":
            	if (partidos == null || partidos >= 20) {
            		return challenger.getOrDefault(criterio, 0);
            	} else {
            		return challenger.getOrDefault(criterio, 0)/2;
            	}
            default:
                return 0;
        }
    }
}
