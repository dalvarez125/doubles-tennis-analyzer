package com.tennis.doubles.service.carga.partidos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennis.doubles.dto.carga.partidos.EquipoDTO;
import com.tennis.doubles.dto.carga.partidos.EquipoScoreDTO;
import com.tennis.doubles.dto.carga.partidos.EventoDTO;
import com.tennis.doubles.dto.carga.partidos.RespuestaEventosDTO;
import com.tennis.doubles.dto.carga.partidos.SubEquipoDTO;
import com.tennis.doubles.model.*;
import com.tennis.doubles.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CargadorPartidosService {

	private final ObjectMapper objectMapper;
    private final JugadorRepository jugadorRepository;
    private final ParejaRepository parejaRepository;
    private final PartidoRepository partidoRepository;
    private final TorneoRepository torneoRepository;

    public void cargarDesdeArchivoJson() {
    	try (InputStream is = getClass().getResourceAsStream("/data/partidos.json")) {
            RespuestaEventosDTO respuesta = objectMapper.readValue(is, RespuestaEventosDTO.class);
            
            for (EventoDTO evento : respuesta.getEvents()) {
            	if (!esDobles(evento.getHomeTeam()) || !esDobles(evento.getAwayTeam()) 
            			|| esDoblesMixtos(evento.getHomeTeam()) || esDoblesMixtos(evento.getAwayTeam()) 
            			|| !esAtpWtaItf(evento) || !finalizado(evento)) continue;

                // Procesar torneo
            	String slugTorneo = evento.getTournament().getSlug(); // Ej: "roland-garros"
            	int anio = Integer.parseInt(evento.getSeason().getYear().trim());
            	String idTorneo = slugTorneo + "-" + anio;

            	String nombreTorneo = evento.getTournament().getName();
            	String superficie = evento.getTournament().getUniqueTournament().getGroundType();
                Torneo torneo = torneoRepository.findByNombreAndAnio(nombreTorneo, anio)
                        .orElseGet(() -> {
                            Torneo nuevoTorneo = new Torneo(idTorneo, nombreTorneo, superficie, evento.getTournament().getCategory().getName(), anio);
                            return torneoRepository.save(nuevoTorneo);
                        });

                // Procesar jugadores
                Jugador jugador1 = guardarJugador(evento.getHomeTeam().getSubTeams().get(0), evento.getTournament().getCategory().getName());
                Jugador jugador2 = guardarJugador(evento.getHomeTeam().getSubTeams().get(1), evento.getTournament().getCategory().getName());
                Jugador jugador3 = guardarJugador(evento.getAwayTeam().getSubTeams().get(0), evento.getTournament().getCategory().getName());
                Jugador jugador4 = guardarJugador(evento.getAwayTeam().getSubTeams().get(1), evento.getTournament().getCategory().getName());

                // Procesar parejas
                Pareja parejaLocal = guardarPareja(jugador1, jugador2, evento.getHomeTeam().getId());
                Pareja parejaVisitante = guardarPareja(jugador3, jugador4, evento.getAwayTeam().getId());

             // Obtener fecha del evento
                LocalDate fechaEvento = Instant.ofEpochSecond(evento.getStartTimestamp())
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDate();

                // Construir marcador (ej. "6-4, 3-6, 10-8")
                List<String> sets = new ArrayList<String>();
                EquipoScoreDTO localScore = evento.getHomeScore();
                EquipoScoreDTO visitanteScore = evento.getAwayScore();

                for (int i = 1; i <= 5; i++) {
                    Integer setLocal = getPeriodo(localScore, i);
                    Integer setVisitante = getPeriodo(visitanteScore, i);
                    if (setLocal != null && setVisitante != null) {
                        sets.add(setLocal + "-" + setVisitante);
                    }
                }
                String marcador = String.join(", ", sets);

                // Determinar pareja ganadora y perdedora
                Pareja parejaGanadora = null;
                Pareja parejaPerdedora = null;

                if (evento.getWinnerCode() != null) {
                    if (evento.getWinnerCode() == 1) {
                        parejaGanadora = parejaLocal;
                        parejaPerdedora = parejaVisitante;
                    } else if (evento.getWinnerCode() == 2) {
                        parejaGanadora = parejaVisitante;
                        parejaPerdedora = parejaLocal;
                    }
                }

                // Crear y guardar el partido
                Partido partido = new Partido();
                partido.setId(evento.getId());
                partido.setFecha(fechaEvento);
                try {
                partido.setRonda(evento.getRoundInfo().getName());
                } catch (Exception e) {
                	Files.writeString(Path.of("error_log.log"), "Error en el partido: " + evento.getId() + "\n", StandardOpenOption.APPEND);
                }
                partido.setMarcador(marcador);
                partido.setParejaGanadora(parejaGanadora);
                partido.setParejaPerdedora(parejaPerdedora);
                partido.setTorneo(torneo);

                partidoRepository.findById(evento.getId()).orElseGet(() -> partidoRepository.save(partido));
            }

        } catch (IOException e) {
            log.error("Error al leer el archivo de partidos", e);
        }
    }

    private boolean finalizado(EventoDTO evento) {
		return evento.getStatus() != null && evento.getStatus().getCode() == 100;
	}

	private boolean esAtpWtaItf(EventoDTO evento) {
    	
    	return evento.getTournament().getCategory().getName().equals("ATP") || evento.getTournament().getCategory().getName().equals("WTA") || 
    			evento.getTournament().getCategory().getName().equals("ITF Men") || evento.getTournament().getCategory().getName().equals("ITF Women");
	}

	private Jugador guardarJugador(SubEquipoDTO dto, String categoria) {
        return jugadorRepository.findByNombre(dto.getName())
                .orElseGet(() -> jugadorRepository.save(new Jugador(dto.getId(), dto.getName(), null, categoria)));
    }

    private Pareja guardarPareja(Jugador j1, Jugador j2, Long idPareja) {
        Optional<Pareja> existente = parejaRepository.findByJugadoresIgnoreOrder(j1, j2);
        return existente.orElseGet(() -> parejaRepository.save(new Pareja(idPareja, j1, j2, false)));
    }
    
    private Integer getPeriodo(EquipoScoreDTO score, int periodo) {
        switch (periodo) {
            case 1: return score.getPeriod1();
            case 2: return score.getPeriod2();
            case 3: return score.getPeriod3();
            case 4: return score.getPeriod4();
            case 5: return score.getPeriod5();
            default: return null;
        }
    }
    
    private boolean esDoblesMixtos(EquipoDTO equipo) {
        if (equipo.getSubTeams() == null || equipo.getSubTeams().size() != 2) {
            return true; // No es una pareja válida
        }

        String genero1 = equipo.getSubTeams().get(0).getGender();
        String genero2 = equipo.getSubTeams().get(1).getGender();

        return genero1 != null && genero2 != null && !genero1.equals(genero2);
    }
    
    private boolean esDobles(EquipoDTO equipo) {
    	return equipo.getType() == 2;
    }

	public void cargaListaEventos(List<EventoDTO> listaEventos, LocalDate fecha) {
		for (EventoDTO evento : listaEventos) {
        	if (!esDobles(evento.getHomeTeam()) || !esDobles(evento.getAwayTeam()) 
        			|| esDoblesMixtos(evento.getHomeTeam()) || esDoblesMixtos(evento.getAwayTeam()) 
        			|| !esAtpWtaItf(evento) || !finalizado(evento) || evento.getRoundInfo() == null) continue;

        	 try {
            
	        	// Procesar torneo
	        	String slugTorneo = evento.getTournament().getSlug(); // Ej: "roland-garros"
	        	int anio = Integer.parseInt(evento.getSeason().getYear().trim());
	        	String idTorneo = slugTorneo + "-" + anio;
	
	        	String nombreTorneo = evento.getTournament().getName();
	        	String superficie = evento.getTournament().getUniqueTournament().getGroundType();
	            Torneo torneo = torneoRepository.findByNombreAndAnio(nombreTorneo, anio)
	                    .orElseGet(() -> {
	                        Torneo nuevoTorneo = new Torneo(idTorneo, nombreTorneo, superficie, evento.getTournament().getCategory().getName(), anio);
	                        return torneoRepository.save(nuevoTorneo);
	                    });
	            
	            String categoriaJugador = evento.getTournament().getCategory().getName();
	            if (categoriaJugador.equals("ITF Men")) {
	            	categoriaJugador = "ATP";
	            } else {
	            	if (categoriaJugador.equals("ITF Women")) {
		            	categoriaJugador = "WTA";
		            }
	            }
	
	            // Procesar jugadores
	            Jugador jugador1 = guardarJugador(evento.getHomeTeam().getSubTeams().get(0), evento.getTournament().getCategory().getName());
	            Jugador jugador2 = guardarJugador(evento.getHomeTeam().getSubTeams().get(1), evento.getTournament().getCategory().getName());
	            Jugador jugador3 = guardarJugador(evento.getAwayTeam().getSubTeams().get(0), evento.getTournament().getCategory().getName());
	            Jugador jugador4 = guardarJugador(evento.getAwayTeam().getSubTeams().get(1), evento.getTournament().getCategory().getName());
	
	            // Procesar parejas
	            Pareja parejaLocal = guardarPareja(jugador1, jugador2, evento.getHomeTeam().getId());
	            Pareja parejaVisitante = guardarPareja(jugador3, jugador4, evento.getAwayTeam().getId());
	
	         // Obtener fecha del evento
	            LocalDate fechaEvento = Instant.ofEpochSecond(evento.getStartTimestamp())
	                                            .atZone(ZoneId.systemDefault())
	                                            .toLocalDate();
	
	            // Construir marcador (ej. "6-4, 3-6, 10-8")
	            List<String> sets = new ArrayList<String>();
	            EquipoScoreDTO localScore = evento.getHomeScore();
	            EquipoScoreDTO visitanteScore = evento.getAwayScore();
	
	            for (int i = 1; i <= 5; i++) {
	                Integer setLocal = getPeriodo(localScore, i);
	                Integer setVisitante = getPeriodo(visitanteScore, i);
	                if (setLocal != null && setVisitante != null) {
	                    sets.add(setLocal + "-" + setVisitante);
	                }
	            }
	            String marcador = String.join(", ", sets);
	
	            // Determinar pareja ganadora y perdedora
	            Pareja parejaGanadora = null;
	            Pareja parejaPerdedora = null;
	
	            if (evento.getWinnerCode() != null) {
	                if (evento.getWinnerCode() == 1) {
	                    parejaGanadora = parejaLocal;
	                    parejaPerdedora = parejaVisitante;
	                } else if (evento.getWinnerCode() == 2) {
	                    parejaGanadora = parejaVisitante;
	                    parejaPerdedora = parejaLocal;
	                }
	            }
	            
	            // Crear y guardar el partido
	            Partido partido = new Partido();
	            partido.setId(evento.getId());
	            partido.setFecha(fechaEvento);
	            partido.setRonda(evento.getRoundInfo().getName());
	            partido.setMarcador(marcador);
	            partido.setParejaGanadora(parejaGanadora);
	            partido.setParejaPerdedora(parejaPerdedora);
	            partido.setTorneo(torneo);
	
	            partidoRepository.findById(evento.getId()).orElseGet(() -> partidoRepository.save(partido));
            
        	 } catch (Exception e) {
             	try {
 					Files.writeString(Path.of("error_log.log"), "Error en el partido: " + evento.getId()+ " : " + fecha + " : " + e.getMessage() +  "\n", StandardOpenOption.APPEND);
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
             }
        }
		
	}
}
