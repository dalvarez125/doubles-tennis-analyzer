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

    public int getPeso(String categoria, String criterio) {
        if (categoria == null || criterio == null) return 0;

        categoria = categoria.toLowerCase();
        criterio = criterio.toLowerCase();

        switch (categoria) {
            case "atp":
            case "wta":
                return atp.getOrDefault(criterio, 0);
            case "itf men":
            case "itf women":
            case "itf":
                return itf.getOrDefault(criterio, 0);
            default:
                return 0;
        }
    }
}
