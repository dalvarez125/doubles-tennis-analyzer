package com.tennis.doubles.service.detalle;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tennis.doubles.dto.detalle.DetalleParejaDTO;
import com.tennis.doubles.repository.ParejaRepository;

@Service
public class DetalleService {

    private final ParejaRepository parejaRepository;

    public DetalleService(ParejaRepository parejaRepository) {
        this.parejaRepository = parejaRepository;
    }

    public List<DetalleParejaDTO> obtenerDetallePareja(Long parejaId, LocalDate fechaInicio, LocalDate fechaFin, String superficie) {
        return parejaRepository.obtenerDetallePareja(parejaId, fechaInicio, fechaFin, superficie);
    }
}
