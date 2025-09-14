package com.tennis.doubles.dto.rankings;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RankingJugadorDTO {
    private Long id;
    private String nombre;
    private int victorias;
    private int derrotas;
    private Long totalPartidos;
    private double porcentaje;
    
    public RankingJugadorDTO(Long id, String nombre, BigDecimal victorias, BigDecimal derrotas, Long totalPartidos, BigDecimal porcentaje) {
		this.id = id;
		this.nombre = nombre;
		this.victorias = victorias != null ? victorias.intValue() : 0;
		this.derrotas = derrotas != null ? derrotas.intValue() : 0;
		this.totalPartidos = totalPartidos;
		this.porcentaje = porcentaje != null ? porcentaje.doubleValue() : 0.0;
	}
    
    
}
