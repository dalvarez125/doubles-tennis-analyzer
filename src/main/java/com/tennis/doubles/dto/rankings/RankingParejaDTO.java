package com.tennis.doubles.dto.rankings;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RankingParejaDTO {
    private Long id;
    private String jugador1;
    private String jugador2;
    private int victorias;
    private int derrotas;
    private Long totalPartidos;
    private double porcentaje;
    private boolean favorito;
    
    public RankingParejaDTO(Long id, String jugador1, String jugador2, BigDecimal victorias, BigDecimal derrotas, BigDecimal totalPartidos, BigDecimal porcentaje, boolean favorito) {
		this.id = id;
		this.jugador1 = jugador1;
		this.jugador2 = jugador2;
		this.victorias = victorias != null ? victorias.intValue() : 0;
		this.derrotas = derrotas != null ? derrotas.intValue() : 0;
		this.totalPartidos = totalPartidos != null ? totalPartidos.longValue() : 0;
		this.porcentaje = porcentaje != null ? porcentaje.doubleValue() : 0.0;
		this.favorito = favorito;
	}
    
    
}
