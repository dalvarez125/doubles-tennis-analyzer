package com.tennis.doubles.dto.carga.partidos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubEquipoDTO {
	private Long id;
    private String name;
    private String gender;
}
