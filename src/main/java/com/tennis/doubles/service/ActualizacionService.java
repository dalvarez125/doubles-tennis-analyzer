package com.tennis.doubles.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tennis.doubles.model.Actualizacion;
import com.tennis.doubles.repository.ActualizacionRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Service
public class ActualizacionService {

    private final ActualizacionRepository actualizacionRepository;

    public ActualizacionService(ActualizacionRepository actualizacionRepository) {
        this.actualizacionRepository = actualizacionRepository;
    }

    /**
     * Comprueba si es necesario actualizar (si la última fecha < lunes pasado).
     */
    public boolean necesitaActualizar() {
        LocalDate hoy = LocalDate.now();
        LocalDate ultimoLunes = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        Actualizacion ultima = actualizacionRepository.findTopByOrderByFechaUltimaActualizacionDesc();
        if (ultima == null) {
            // nunca se actualizó, hay que actualizar
            return true;
        }
        return ultima.getFechaUltimaActualizacion().isBefore(ultimoLunes);
    }

    @Transactional
    public void guardarResultado(LocalDate fecha, String estado) {
        Actualizacion act = new Actualizacion();
        act.setFechaUltimaActualizacion(fecha);
        act.setEstado(estado);
        actualizacionRepository.save(act);
    }

	public ActualizacionRepository getActualizacionRepository() {
		return actualizacionRepository;
	}
    
}
