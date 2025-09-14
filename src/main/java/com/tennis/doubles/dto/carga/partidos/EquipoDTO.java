package com.tennis.doubles.dto.carga.partidos;

import lombok.Data;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EquipoDTO {
    private String name;
    private String gender;
    private List<SubEquipoDTO> subTeams;
    private int type;
    private Long id;
}
