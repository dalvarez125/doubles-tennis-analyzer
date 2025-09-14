package com.tennis.doubles.dto.carga.ranking;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RankingDTO {
	private String name;
	
	@JsonProperty("Name")
    private String nameAtp;
	private int ranking;
}
