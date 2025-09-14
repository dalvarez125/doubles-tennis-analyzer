package com.tennis.doubles.dto.carga.partidos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TorneoUnicoDTO {
    private String name;
    private String groundType;
}
