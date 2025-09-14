package com.tennis.doubles.dto.carga.partidos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventoDTO {
	private Long startTimestamp;
	
    private TorneoDTO tournament;
    private TemporadaDTO season;
    private RondaDTO roundInfo;
    private EquipoDTO homeTeam;
    private EquipoDTO awayTeam;
    private EquipoScoreDTO homeScore;
    private EquipoScoreDTO awayScore;
    private Integer winnerCode;
    private EstadoPartidoDTO status;
    private Long id;
}
