package com.tennis.doubles.dto.estadisticas;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PronosticoPartidoDTO {
    private Long partidoId;
    private String fecha;
    private String torneo;
    private String parejaLocal;
    private String parejaVisitante;
    private String parejaPronosticada;
    private String resultado;
    private boolean acierto;
    private String categoria;
    private boolean femenino;
    private Double cuota;
}
