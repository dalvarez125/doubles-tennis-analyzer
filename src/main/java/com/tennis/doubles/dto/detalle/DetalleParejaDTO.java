package com.tennis.doubles.dto.detalle;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.sql.Date;

@Data
@NoArgsConstructor
public class DetalleParejaDTO {
    private LocalDate fecha;
    private String rivales;
    private String marcador;
    private String resultado;
    private String torneo;
    private String superficie;
    private Long partidoId;
    private String pareja;
    private String ronda;
    
	public DetalleParejaDTO(Date fecha, String rivales, String marcador, String resultado, String torneo,
			String superficie, Long partidoId, String pareja, String ronda) {
		super();
		this.fecha = fecha.toLocalDate();
		this.rivales = rivales;
		this.marcador = marcador;
		this.resultado = resultado;
		this.torneo = torneo;
		this.superficie = superficie;
		this.partidoId = partidoId;
		this.pareja = pareja;
		this.ronda = ronda;
	}
    
    
}
