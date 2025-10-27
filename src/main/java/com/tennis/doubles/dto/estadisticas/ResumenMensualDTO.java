package com.tennis.doubles.dto.estadisticas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenMensualDTO {
    private int totalPronosticos;
    private int aciertos;
    private int fallos;
    private double porcentajeAcierto;
}
