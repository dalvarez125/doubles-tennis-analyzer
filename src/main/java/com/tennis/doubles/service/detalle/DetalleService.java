package com.tennis.doubles.service.detalle;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tennis.doubles.dto.detalle.DetalleJugadorDTO;
import com.tennis.doubles.dto.detalle.DetalleParejaDTO;
import com.tennis.doubles.repository.JugadorRepository;
import com.tennis.doubles.repository.ParejaRepository;

@Service
public class DetalleService {

    private final ParejaRepository parejaRepository;
    
    private final JugadorRepository jugadorRepository;

    public DetalleService(ParejaRepository parejaRepository, JugadorRepository jugadorRepository) {
        this.parejaRepository = parejaRepository;
		this.jugadorRepository = jugadorRepository;
    }

    public List<DetalleParejaDTO> obtenerDetallePareja(Long parejaId, LocalDate fechaInicio, LocalDate fechaFin, String superficie) {
        return parejaRepository.obtenerDetallePareja(parejaId, fechaInicio, fechaFin, superficie);
    }

	public List<DetalleJugadorDTO> obtenerDetalleJugador(Long jugadorId, LocalDate fechaInicio, LocalDate fechaFin,
			String superficie) {
		return jugadorRepository.obtenerDetalleJugador(jugadorId, fechaInicio, fechaFin, superficie);
	}
}
