package com.tennis.doubles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.tennis.doubles.dto.carga.ranking.RankingDTO;
import com.tennis.doubles.model.Jugador;
import com.tennis.doubles.repository.JugadorEstadisticasRepository;
import com.tennis.doubles.repository.JugadorRepository;
import com.tennis.doubles.repository.ParejaEstadisticasRepository;
import com.tennis.doubles.service.ActualizacionService;
import com.tennis.doubles.service.JugadorService;
import com.tennis.doubles.service.carga.partidos.AllSportsApiService;
import com.tennis.doubles.service.carga.partidos.CargadorPartidosService;
import com.tennis.doubles.service.carga.partidos.UltimateTennisService;

@Component
public class DataLoader implements CommandLineRunner {

    private final CargadorPartidosService cargadorPartidosService;
    private final ConfiguracionCarga configuracionCarga;
    private final AllSportsApiService allSportsApiService;
    private final UltimateTennisService ultimateTennisService;
    private final JugadorRepository jugadorRepository;
    private final ActualizacionService actualizacionService;
    private final JugadorEstadisticasRepository jugadorEstadisticasRepository;
    private final ParejaEstadisticasRepository parejaEstadisticasRepository;
    private final PesosComparativaConfig pesosComparativaConfig;

    public DataLoader(CargadorPartidosService cargadorPartidosService, ConfiguracionCarga configuracionCarga, AllSportsApiService allSportsApiService, UltimateTennisService ultimateTennisService, JugadorRepository jugadorRepository, ActualizacionService actualizacionService, PesosComparativaConfig pesosComparativaConfig, ParejaEstadisticasRepository parejaEstadisticasRepository, JugadorEstadisticasRepository jugadorEstadisticasRepository) {

        this.cargadorPartidosService = cargadorPartidosService;
		this.configuracionCarga = configuracionCarga;
		this.allSportsApiService = allSportsApiService;
		this.ultimateTennisService = ultimateTennisService;
		this.jugadorRepository = jugadorRepository;
		this.actualizacionService = actualizacionService;
		this.jugadorEstadisticasRepository = jugadorEstadisticasRepository;
		this.parejaEstadisticasRepository = parejaEstadisticasRepository;
		this.pesosComparativaConfig = pesosComparativaConfig;
    }

    @Override
    public void run(String... args) {
//    	LocalDate fechaInicio = configuracionCarga.getInicio();
//    	LocalDate fechaFin = configuracionCarga.getFin();
//    	for (LocalDate fecha = fechaInicio; !fecha.isAfter(fechaFin); fecha = fecha.plusDays(1)) {
//
//		    try {
//		    	cargadorPartidosService.cargaListaEventos(allSportsApiService.obtenerEventosPorFecha(fecha), fecha);
//		    	Files.writeString(Path.of("carga_log.log"), "Fecha cargada " + fecha + "\n", StandardOpenOption.APPEND);
//		    } catch (Exception e) {
//		    	try {
//					Files.writeString(Path.of("error_log.log"), "Error al procesar la fecha " + fecha + ": " + e.getMessage() + "\n", StandardOpenOption.APPEND);
//				} catch (IOException e1) {
//			        System.err.println("Error al procesar la fecha " + fecha + ": " + e.getMessage());
//				}
//
//		    }
//		}
//    	jugadorEstadisticasRepository.borrarEstadisticasJugadores();
//    	jugadorEstadisticasRepository.insertEstadisticasJugadores();
//    	parejaEstadisticasRepository.borrarEstadisticasParejas();
//    	parejaEstadisticasRepository.insertEstadisticasParejas();
    	
    	if (actualizacionService.necesitaActualizar()) {
    		LocalDate hoy = LocalDate.now();
            LocalDate ultimaActualizacion = actualizacionService.getActualizacionRepository().findTopByOrderByFechaUltimaActualizacionDesc().getFechaUltimaActualizacion();
            boolean errorPartidos = false;
            boolean errorRanking = false;
//            for (LocalDate fecha = ultimaActualizacion;
//   			     !fecha.isAfter(hoy.minusDays(1));
//   			     fecha = fecha.plusDays(1)) {
//   	
//   			    try {
//   			    	cargadorPartidosService.cargaListaEventos(allSportsApiService.obtenerEventosPorFecha(fecha));
//   			    	Files.writeString(Path.of("carga_log.log"), "Fecha cargada " + fecha + "\n", StandardOpenOption.APPEND);
//   			    } catch (Exception e) {
//   			    	try {
//   						Files.writeString(Path.of("error_log.log"), "Error al procesar la fecha " + fecha + ": " + e.getMessage() + "\n", StandardOpenOption.APPEND);
//   						errorPartidos = true;
//   					} catch (IOException e1) {
//   				        System.err.println("Error al procesar la fecha " + fecha + ": " + e.getMessage());
//   					}
//   	
//   			    }
//   			}
	    	jugadorRepository.resetearRankingDobles();
	    	try {
				List<RankingDTO> rankingATP = ultimateTennisService.obtenerRanking("atp");
				int numeroRanking = 1;
				for (RankingDTO ranking : rankingATP) {
					String[] partesNombre = ranking.getNameAtp().trim().split("\\.");
			        if (partesNombre.length == 2) {
			            String inicial = partesNombre[0].trim();
			            String apellido = partesNombre[1].trim();
			            if (apellido.equals("Puetz")) {
			            	apellido = "Putz";
			            }
			            List<Jugador> listaJugador = jugadorRepository.buscarPorInicialYApellido(inicial, apellido);
			            if (!listaJugador.isEmpty()) {
			            	if (listaJugador.size() > 1) {
			            		Files.writeString(Path.of("error_log.log"), "Jugador duplicado ranking " + numeroRanking + ": " + inicial + ". " + apellido + "\n", StandardOpenOption.APPEND);
			            		errorRanking = true;
			            	}
			                Jugador jugador = listaJugador.get(0);
			                jugador.setRankingDobles(numeroRanking);
			                jugadorRepository.save(jugador);
			            } else {
			            	Files.writeString(Path.of("error_log.log"), "Jugador no encontrado " + numeroRanking + ": " + inicial + ". " + apellido + "\n", StandardOpenOption.APPEND);
		            		errorRanking = true;
			            }
			        }
			        numeroRanking++;
				}
			} catch (IOException | InterruptedException e) {
				try {
					Files.writeString(Path.of("error_log.log"), "Error al obtener ranking ATP: " + e.getMessage() + "\n", StandardOpenOption.APPEND);
					errorRanking = true;
				} catch (IOException e1) {
			        System.err.println("Error al obtener ranking ATP: " + e.getMessage());
				}
			}
	    	try {
	    		List<RankingDTO> rankingWTA = ultimateTennisService.obtenerRanking("wta");
	    		for (RankingDTO ranking : rankingWTA) {
	    			jugadorRepository.findByNombre(ranking.getName())
	                .ifPresent(jugador -> {
	                    jugador.setRankingDobles(ranking.getRanking());
	                    jugadorRepository.save(jugador);
	                });
				}
			} catch (IOException | InterruptedException e) {
				try {
					Files.writeString(Path.of("error_log.log"), "Error al obtener ranking ATP: " + e.getMessage() + "\n", StandardOpenOption.APPEND);
					errorRanking = true;
				} catch (IOException e1) {
			        System.err.println("Error al obtener ranking ATP: " + e.getMessage());
				}
			}
	    	
	    	String estado = "OK";
	    	if (errorPartidos && errorRanking) {
	    		estado = "KO";
	    	} else {
	    		if (errorPartidos || errorRanking) {
	    			estado = "PARCIAL";
	    		}
	    	}
	    	actualizacionService.guardarResultado(hoy, estado);
    	}
    }
}
