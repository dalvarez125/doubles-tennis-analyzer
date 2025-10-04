package com.tennis.doubles.service;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tennis.doubles.dto.carga.partidos.ProximosPartidosDTO;
import com.tennis.doubles.dto.estadisticas.EstadisticasPronosticosDTO;
import com.tennis.doubles.dto.estadisticas.PronosticoPartidoDTO;
import com.tennis.doubles.model.Partido;
import com.tennis.doubles.model.PrediccionPartido;
import com.tennis.doubles.repository.JugadorRepository;
import com.tennis.doubles.repository.ParejaRepository;
import com.tennis.doubles.repository.PartidoRepository;
import com.tennis.doubles.repository.PrediccionPartidoRepository;

@Service
public class PrediccionPartidoService {
	
	@Value("${env-dev}")
    private boolean envDev;

    private final PrediccionPartidoRepository prediccionPartidoRepository;
    private final PartidoRepository partidoRepository;
    private final ParejaRepository parejaRepository;
    private final JugadorRepository jugadorRepository;

    public PrediccionPartidoService(PrediccionPartidoRepository prediccionPartidoRepository,
                                    PartidoRepository partidoRepository,
                                    ParejaRepository parejaRepository, JugadorRepository jugadorRepository) {
        this.prediccionPartidoRepository = prediccionPartidoRepository;
        this.partidoRepository = partidoRepository;
        this.parejaRepository = parejaRepository;
		this.jugadorRepository = jugadorRepository;
    }

    @Transactional
    public void guardarPrediccion(ProximosPartidosDTO dto) {
    	if (envDev) {
	    	PrediccionPartido prediccion = prediccionPartidoRepository.findById(dto.getId())
	    	        .orElseGet(PrediccionPartido::new);
	        prediccion.setId(dto.getId());
	        prediccion.setParejaGanadoraId(
	                dto.getComparativa().getPuntosLocal() > dto.getComparativa().getPuntosVisitante()
	                        ? dto.getParejaLocalId()
	                        : dto.getParejaVisitanteId()
	        );
	        prediccion.setPuntosLocal(dto.getComparativa().getPuntosLocal());
	        prediccion.setPuntosVisitante(dto.getComparativa().getPuntosVisitante());
	        
	        if (prediccion.getCuota() == null) {
	        	prediccion.setCuota(dto.getComparativa().getPuntosLocal() > dto.getComparativa().getPuntosVisitante()
	                        ? dto.getCuotaLocal()
	                        : dto.getCuotaVisitante());
	        }
	
	        prediccionPartidoRepository.save(prediccion);
    	}
    }

    public List<PronosticoPartidoDTO> obtenerPredicciones(LocalDate desde, LocalDate hasta) {
        List<PrediccionPartido> predicciones = prediccionPartidoRepository.findPrediccionesEntreFechas(desde, hasta);

        return predicciones.stream().map(p -> {
            Partido partido = partidoRepository.findById(p.getId()).orElseThrow();

            String parejaLocal = partido.getParejaGanadora().getJugador1().getNombre() + " / " +
                                 partido.getParejaGanadora().getJugador2().getNombre();

            String parejaVisitante = partido.getParejaPerdedora().getJugador1().getNombre() + " / " +
                                     partido.getParejaPerdedora().getJugador2().getNombre();
            
            String parejaPronosticada = parejaRepository.findById(p.getParejaGanadoraId()) .map(pa -> pa.getJugador1().getNombre() + " / " + pa.getJugador2().getNombre()) .orElse("Desconocida") + " <br>(" + p.getPuntosLocal() + "-" + p.getPuntosVisitante() + ")";
            
            String resultado = partido.getMarcador(); // El marcador como columna “resultado”
            boolean acierto = partido.getParejaGanadora().getId().equals(p.getParejaGanadoraId());
            
            boolean isFemenino = jugadorRepository.isFemenino(partido.getParejaGanadora().getJugador1().getId());

            return new PronosticoPartidoDTO(
                    partido.getId(),
                    partido.getFecha().toString(),
                    partido.getTorneo().getNombre(),
                    parejaLocal,
                    parejaVisitante,
                    parejaPronosticada,
                    resultado,
                    acierto, 
                    partido.getTorneo().getCategoria(),
                    isFemenino,
                    p.getCuota()
            );
        }).collect(Collectors.toList());
    }

    public EstadisticasPronosticosDTO calcularEstadisticas(List<PronosticoPartidoDTO> predicciones) {
        EstadisticasPronosticosDTO resumen = new EstadisticasPronosticosDTO();

        resumen.setTotalPronosticos(predicciones.size());
        resumen.setTotalAciertos(predicciones.stream().filter(PronosticoPartidoDTO::isAcierto).count());
        resumen.setTotalFallos(resumen.getTotalPronosticos() - resumen.getTotalAciertos());
        resumen.setPorcentajeAcierto(resumen.getTotalPronosticos() > 0
                ? resumen.getTotalAciertos() * 100.0 / resumen.getTotalPronosticos()
                : 0.0);

        return resumen;
    }
    
    public EstadisticasPronosticosDTO calcularEstadisticasPorFiltro(
            List<PronosticoPartidoDTO> predicciones, 
            Predicate<PronosticoPartidoDTO> filtro) {
        List<PronosticoPartidoDTO> filtradas = predicciones.stream().filter(filtro).toList();
        long total = filtradas.size();
        long aciertos = filtradas.stream().filter(PronosticoPartidoDTO::isAcierto).count();
        long fallos = total - aciertos;
        double porcentaje = total > 0 ? (aciertos * 100.0 / total) : 0.0;
        return new EstadisticasPronosticosDTO(total, aciertos, fallos, porcentaje);
    }
    
}
