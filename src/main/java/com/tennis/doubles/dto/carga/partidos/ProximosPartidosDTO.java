package com.tennis.doubles.dto.carga.partidos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProximosPartidosDTO {
	private Long id;
	
    private LocalDate fecha;
    private LocalTime hora;
    private String torneo;
    private String superficie;
    private String ronda;
    private Long parejaLocalId;
    private String parejaLocalNombre;
    private Long parejaVisitanteId;
    private String parejaVisitanteNombre;
    private String categoria;
    private Long jugador1Id;
    private Long jugador2Id;
    private Long jugador3Id;
    private Long jugador4Id;
    
    private ResumenComparativaDTO comparativa;
    private String estiloComparativa;
}
