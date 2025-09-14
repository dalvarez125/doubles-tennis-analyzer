package com.tennis.doubles.dto.carga.ranking;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RankingResponse {
    private String category;
    private List<RankingDTO> data;
}
