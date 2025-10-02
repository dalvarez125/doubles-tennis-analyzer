package com.tennis.doubles.dto.estadisticas;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasPronosticosDTO {
	private long totalPronosticos;
    private long totalAciertos;
    private long totalFallos;
    private double porcentajeAcierto;
}
