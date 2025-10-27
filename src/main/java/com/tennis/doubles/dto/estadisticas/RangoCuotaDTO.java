package com.tennis.doubles.dto.estadisticas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RangoCuotaDTO {
    private String rango;      // "1,00-1,10", "1,10-1,20", "+1,50", "Sin cuota"
    private long aciertos;
    private long fallos;
    private double porcentajeAcierto;
}
