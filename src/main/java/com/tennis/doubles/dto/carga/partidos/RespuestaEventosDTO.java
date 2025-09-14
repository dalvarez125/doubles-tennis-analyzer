package com.tennis.doubles.dto.carga.partidos;

import lombok.Data;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespuestaEventosDTO {
    private List<EventoDTO> events;
}
