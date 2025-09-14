package com.tennis.doubles.dto.carga.partidos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TorneoDTO {
    private String name;
    private String slug;
    private CategoriaDTO category;
    private TorneoUnicoDTO uniqueTournament;
}
