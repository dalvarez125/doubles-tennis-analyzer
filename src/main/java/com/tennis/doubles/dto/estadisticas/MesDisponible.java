package com.tennis.doubles.dto.estadisticas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MesDisponible {
    private int anio;
    private int mes;        // 1..12
    private String nombre;  // "octubre"
    private String valor;   // "2025-10" (útil si quieres enviar un solo valor)
}
