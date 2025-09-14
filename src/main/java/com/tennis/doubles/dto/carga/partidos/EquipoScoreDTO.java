package com.tennis.doubles.dto.carga.partidos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class EquipoScoreDTO {
    private Integer current;
    private Integer display;

    private Integer period1;
    private Integer period2;
    private Integer period3;
    private Integer period4;
    private Integer period5;

    private String point;
    private Integer normaltime;
}