package com.tennis.doubles.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.tennis.doubles.PesosComparativaConfig;
import com.tennis.doubles.dto.carga.partidos.EquipoDTO;
import com.tennis.doubles.dto.carga.partidos.EventoDTO;
import com.tennis.doubles.dto.carga.partidos.ProximosPartidosDTO;
import com.tennis.doubles.dto.carga.partidos.ResumenComparativaDTO;
import com.tennis.doubles.dto.carga.partidos.SubEquipoDTO;
import com.tennis.doubles.dto.cuotas.CuotasDTO;
import com.tennis.doubles.dto.cuotas.OpcionCuotaDTO;
import com.tennis.doubles.model.Jugador;
import com.tennis.doubles.model.Pareja;
import com.tennis.doubles.model.EstadisticasJugador;
import com.tennis.doubles.model.EstadisticasPareja;
import com.tennis.doubles.model.Torneo;
import com.tennis.doubles.repository.JugadorEstadisticasRepository;
import com.tennis.doubles.repository.JugadorRepository;
import com.tennis.doubles.repository.ParejaEstadisticasRepository;
import com.tennis.doubles.repository.ParejaRepository;
import com.tennis.doubles.repository.PartidoRepository;
import com.tennis.doubles.repository.TorneoRepository;
import com.tennis.doubles.service.carga.partidos.AllSportsApiService;
import com.tennis.doubles.utils.Constantes;


@Service
public class PartidoService {
	
	private final AllSportsApiService allSportsApiService;
	private final TorneoRepository torneoRepository;
	private final JugadorRepository jugadorRepository;
	private final ParejaRepository parejaRepository;
	private final PartidoRepository partidoRepository;
	private final PesosComparativaConfig pesosComparativaConfig;
	private final JugadorEstadisticasRepository jugadorEstadisticasRepository;
	private final PrediccionPartidoService prediccionPartidoService;
	private final ParejaEstadisticasRepository parejaEstadisticasRepository;
	
	private Map<String, Map<String, List<ProximosPartidosDTO>>> partidosEnMemoria = new HashMap<String, Map<String, List<ProximosPartidosDTO>>>();
	private LocalDate ultimaActualizacion;
	
	
	
	public PartidoService(AllSportsApiService allSportsApiService, TorneoRepository torneoRepository, JugadorRepository jugadorRepository, ParejaRepository parejaRepository, PartidoRepository partidoRepository, PesosComparativaConfig pesosComparativaConfig, JugadorEstadisticasRepository jugadorEstadisticasRepository, PrediccionPartidoService prediccionPartidoService, ParejaEstadisticasRepository parejaEstadisticasRepository) {
		this.allSportsApiService = allSportsApiService;
		this.torneoRepository = torneoRepository;
		this.jugadorRepository = jugadorRepository;
		this.parejaRepository = parejaRepository;
		this.partidoRepository = partidoRepository;
		this.pesosComparativaConfig = pesosComparativaConfig;
		this.jugadorEstadisticasRepository = jugadorEstadisticasRepository;
		this.prediccionPartidoService = prediccionPartidoService;
		this.parejaEstadisticasRepository = parejaEstadisticasRepository;
	}
	
	public ProximosPartidosDTO obtenerPartidoConComparativa(Long id, String fecha, String categoria) {
		return partidosEnMemoria.get(fecha).get(categoria).stream()
	            .filter(p -> p.getId().equals(id))
	            .collect(Collectors.toList()).get(0);
	}	

	public List<ProximosPartidosDTO> getProximosPartidos(String categoria, boolean soloFavoritos, boolean ordenPronostico, boolean soloCerrados, boolean refrescar, String fecha) {
        if (!partidosEnMemoria.containsKey(fecha) || datosDesactualizados() || refrescar) {
            cargarProximosPartidosDesdeAPI(fecha);
        }
        
        List<Pareja> parejasFavoritas = parejaRepository.findByFavorito(true);
        // Obtener todos los IDs de jugadores favoritos
        Set<Long> idsJugadoresFavoritos = parejasFavoritas.stream()
            .flatMap(p -> Stream.of(p.getJugador1().getId(), p.getJugador2().getId()))
            .collect(Collectors.toSet());
        
        List<ProximosPartidosDTO> respuesta = partidosEnMemoria.get(fecha).get(categoria); 
        
        if (!ordenPronostico) {
        	respuesta = respuesta.stream()
    			    .sorted(Comparator
    			            .comparing(ProximosPartidosDTO::getFecha)
    			            .thenComparing(ProximosPartidosDTO::getHora))
    			        .collect(Collectors.toList());
        } else {
        	respuesta = respuesta.stream()
        		    .sorted(Comparator
    			            .comparing(ProximosPartidosDTO::getFecha).reversed()
    			            .thenComparingInt((ProximosPartidosDTO p) ->
        		            Math.abs(p.getComparativa().getPuntosLocal() - p.getComparativa().getPuntosVisitante()))
        		        .thenComparingInt(p ->
        		            Math.max(p.getComparativa().getPuntosLocal(), p.getComparativa().getPuntosVisitante()))
        		        .reversed())
        		    .collect(Collectors.toList());
        }

        return respuesta.stream()
            .filter(p -> !soloFavoritos || contieneJugadorFavorito(p, idsJugadoresFavoritos))
            .filter(p -> !soloCerrados || !p.getEstiloComparativa().equals(""))
            .collect(Collectors.toList());
    }
	
	//METODOS PRIVADOS
	
	private boolean datosDesactualizados() {
        return ultimaActualizacion == null || !ultimaActualizacion.equals(LocalDate.now());
    }
	
	private void cargarProximosPartidosDesdeAPI(String fecha) {
		//Se realizan 3 llamadas al API para obtener los partidos de los proximos 3 días
		LocalDate localDatefecha = LocalDate.now();
		if (fecha.equals(Constantes.MANANA)){
			localDatefecha = localDatefecha.plusDays(1);
		} else if (fecha.equals(Constantes.PASADO)) {
			localDatefecha = localDatefecha.plusDays(2);
		}
		List<ProximosPartidosDTO> listaPartidosItf = new ArrayList<ProximosPartidosDTO>();
		List<ProximosPartidosDTO> listaPartidosAtp = new ArrayList<ProximosPartidosDTO>();
		List<ProximosPartidosDTO> listaPartidosChallenger = new ArrayList<ProximosPartidosDTO>();
		
		try {
			List<EventoDTO> listaEventos = allSportsApiService.obtenerEventosPorFecha(localDatefecha);
			Map<String,CuotasDTO> mapaCuotas = allSportsApiService.obtenerCuotasPorFecha(localDatefecha);
			for (EventoDTO evento : listaEventos) {
	        	if (!esDobles(evento.getHomeTeam()) || !esDobles(evento.getAwayTeam()) 
	        			|| esDoblesMixtos(evento.getHomeTeam()) || esDoblesMixtos(evento.getAwayTeam()) 
	        			|| !esAtpWtaITFChallenger(evento) || evento.getRoundInfo() == null) continue;

	        	 try {
	            
		        	// Procesar torneo
		        	Long idTorneo = evento.getTournament().getId();
		        	int anio = Integer.parseInt(evento.getSeason().getYear());
		
		        	String nombreTorneo = evento.getTournament().getName();
		        	String superficie = evento.getTournament().getUniqueTournament().getGroundType();
		        	String categoriaTorneo = evento.getTournament().getCategory().getName();
		            torneoRepository.findById(idTorneo)
		                    .orElseGet(() -> {
		                        Torneo nuevoTorneo = new Torneo(idTorneo, nombreTorneo, superficie, categoriaTorneo, anio);
		                        return torneoRepository.save(nuevoTorneo);
		                    });
		
		            // Procesar jugadores
		            String categoriaJugador = categoriaTorneo;
		            if (categoriaJugador.equals(Constantes.ITF_MEN) || categoriaJugador.equals(Constantes.CHALLENGER)) {
		            	categoriaJugador = Constantes.ATP;
		            } else {
		            	if (categoriaJugador.equals(Constantes.ITF_WOMEN) || categoriaJugador.equals(Constantes.WTA_125)) {
			            	categoriaJugador = Constantes.WTA;
			            }
		            }
		            Jugador jugador1 = guardarJugador(evento.getHomeTeam().getSubTeams().get(0), categoriaJugador);
		            Jugador jugador2 = guardarJugador(evento.getHomeTeam().getSubTeams().get(1), categoriaJugador);
		            Jugador jugador3 = guardarJugador(evento.getAwayTeam().getSubTeams().get(0), categoriaJugador);
		            Jugador jugador4 = guardarJugador(evento.getAwayTeam().getSubTeams().get(1), categoriaJugador);
		
		            // Procesar parejas
		            Pareja parejaLocal = guardarPareja(jugador1, jugador2, evento.getHomeTeam().getId());
		            Pareja parejaVisitante = guardarPareja(jugador3, jugador4, evento.getAwayTeam().getId());
		
		            ZonedDateTime zonedDateTime = Instant.ofEpochSecond(evento.getStartTimestamp())
                            .atZone(ZoneId.systemDefault());

					LocalDate fechaEvento = zonedDateTime.toLocalDate();
					LocalTime horaEvento = zonedDateTime.toLocalTime();
		
		            //Si la fecha coincide, se crea el DTO para devolver
		            if (fechaEvento.equals(localDatefecha)) {
		            	ProximosPartidosDTO partido = new ProximosPartidosDTO();
		            	partido.setFecha(fechaEvento);
		            	partido.setHora(horaEvento);
		            	partido.setTorneo(nombreTorneo);
		            	partido.setSuperficie(superficie);
			            partido.setRonda(evento.getRoundInfo().getName());
			            partido.setParejaLocalId(parejaLocal.getId());
			            partido.setParejaLocalNombre(parejaLocal.getJugador1().getNombre() + " / " + parejaLocal.getJugador2().getNombre());
			            partido.setParejaVisitanteId(parejaVisitante.getId());
			            partido.setParejaVisitanteNombre(parejaVisitante.getJugador1().getNombre() + " / " + parejaVisitante.getJugador2().getNombre());
			            partido.setCategoria(evento.getTournament().getCategory().getName());
			            partido.setJugador1Id(jugador1.getId());
			            partido.setJugador2Id(jugador2.getId());
			            partido.setJugador3Id(jugador3.getId());
			            partido.setJugador4Id(jugador4.getId());
			            partido.setId(evento.getId());
			            asignarCuotasAlPartido(partido, mapaCuotas);
			            
			            if (categoriaTorneo.contains(Constantes.ITF)) {
			            	listaPartidosItf.add(partido);
			            } else if (categoriaTorneo.equals(Constantes.CHALLENGER) || categoriaTorneo.equals(Constantes.WTA_125)) {
			            	listaPartidosChallenger.add(partido);
			            } else {
			            	listaPartidosAtp.add(partido);
			            }
			            
		            }
	            
	        	 } catch (Exception e) {
	             	try {
	 					Files.writeString(Path.of("error_log.log"), "Error en el partido: " + evento.getId() + "\n", StandardOpenOption.APPEND);
	 				} catch (IOException e1) {
	 					e1.printStackTrace();
	 				}
	             	throw e;
	             }
	        }
		} catch (Exception e) {
			try {
				Files.writeString(Path.of("error_log.log"), "Error al obtener proximos partidos: " + localDatefecha + " - " + e.getMessage() + "\n", StandardOpenOption.APPEND);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		Map<String, List<ProximosPartidosDTO>> mapaFecha = new HashMap<String, List<ProximosPartidosDTO>>();
		
		listaPartidosItf = calcularComparativas(listaPartidosItf);
		listaPartidosChallenger = calcularComparativas(listaPartidosChallenger);
		listaPartidosAtp = calcularComparativas(listaPartidosAtp);
		mapaFecha.put(Constantes.ITF, listaPartidosItf.stream()
			    .sorted(Comparator
			            .comparing(ProximosPartidosDTO::getFecha)
			            .thenComparing(ProximosPartidosDTO::getHora))
			        .collect(Collectors.toList()));
		mapaFecha.put(Constantes.CHALLENGER, listaPartidosChallenger.stream()
			    .sorted(Comparator
			            .comparing(ProximosPartidosDTO::getFecha)
			            .thenComparing(ProximosPartidosDTO::getHora))
			        .collect(Collectors.toList()));
		mapaFecha.put(Constantes.ATP, listaPartidosAtp.stream()
			    .sorted(Comparator
			            .comparing(ProximosPartidosDTO::getFecha)
			            .thenComparing(ProximosPartidosDTO::getHora))
			        .collect(Collectors.toList()));
		
		partidosEnMemoria.put(fecha, mapaFecha);
		
		if (!listaPartidosItf.isEmpty() || !listaPartidosChallenger.isEmpty() || !listaPartidosAtp.isEmpty()) {
			ultimaActualizacion = LocalDate.now();
		}
	}
	
	private boolean esDobles(EquipoDTO equipo) {
    	return equipo.getType() == 2;
    }
	
	private boolean esDoblesMixtos(EquipoDTO equipo) {
        if (equipo.getSubTeams() == null || equipo.getSubTeams().size() != 2) {
            return true;
        }

        String genero1 = equipo.getSubTeams().get(0).getGender();
        String genero2 = equipo.getSubTeams().get(1).getGender();

        return genero1 != null && genero2 != null && !genero1.equals(genero2);
    }
	
	private boolean esAtpWtaITFChallenger(EventoDTO evento) {
    	
    	return evento.getTournament().getCategory().getName().equals(Constantes.ATP) || evento.getTournament().getCategory().getName().equals(Constantes.WTA) ||
    			evento.getTournament().getCategory().getName().equals(Constantes.ITF_MEN) || evento.getTournament().getCategory().getName().equals(Constantes.ITF_WOMEN) ||
    			evento.getTournament().getCategory().getName().equals(Constantes.CHALLENGER) || evento.getTournament().getCategory().getName().equals(Constantes.WTA_125);
	}
	
	private Jugador guardarJugador(SubEquipoDTO dto, String categoria) {
        return jugadorRepository.findById(dto.getId())
                .orElseGet(() -> jugadorRepository.save(new Jugador(dto.getId(), dto.getName(), null, categoria)));
    }
	
	private Pareja guardarPareja(Jugador j1, Jugador j2, Long idPareja) {
        Optional<Pareja> existente = parejaRepository.findById(idPareja);
        return existente.orElseGet(() -> parejaRepository.save(new Pareja(idPareja, j1, j2, false)));
    }
	
	private void asignarCuotasAlPartido(ProximosPartidosDTO partido, Map<String, CuotasDTO> mapaOdds) {
	    if (partido == null || mapaOdds == null) {
	        return;
	    }

	    // Buscar el objeto de cuotas según el id del partido
	    CuotasDTO cuotasDTO = mapaOdds.get(String.valueOf(partido.getId()));
	    if (cuotasDTO == null || cuotasDTO.getChoices() == null) {
	        return;
	    }

	    Double cuotaLocal = null;
	    Double cuotaVisitante = null;

	    for (OpcionCuotaDTO opcion : cuotasDTO.getChoices()) {
	        if ("1".equals(opcion.getName())) {
	            cuotaLocal = convertirFraccionalADecimal(opcion.getFractionalValue());
	        } else if ("2".equals(opcion.getName())) {
	            cuotaVisitante = convertirFraccionalADecimal(opcion.getFractionalValue());
	        }
	    }

	    partido.setCuotaLocal(cuotaLocal);
	    partido.setCuotaVisitante(cuotaVisitante);
	}
	
	private Double convertirFraccionalADecimal(String fraccional) {
	    try {
	        if (fraccional == null || fraccional.isBlank()) return null;

	        String[] partes = fraccional.split("/");
	        if (partes.length != 2) return null;

	        double numerador = Double.parseDouble(partes[0]);
	        double denominador = Double.parseDouble(partes[1]);

	        return (numerador / denominador) + 1.0; // Ejemplo: 8/1 → 9.0
	    } catch (Exception e) {
	        return null;
	    }
	}
	
	private List<ProximosPartidosDTO> calcularComparativas(List<ProximosPartidosDTO> partidosFuturos) {
    	for (ProximosPartidosDTO partido : partidosFuturos) {
        	
            ResumenComparativaDTO comparativa = calcularComparativaPartido(partido);
            partido.setComparativa(comparativa);
            partido.setEstiloComparativa(calcularEstiloComparativa(partido));
            
        }
        return partidosFuturos;
    }
	
	private ResumenComparativaDTO calcularComparativaPartido(ProximosPartidosDTO partido) {

        ResumenComparativaDTO dto = new ResumenComparativaDTO();
        Long parejaLocalId = partido.getParejaLocalId();
        Long parejaVisitanteId = partido.getParejaVisitanteId();

        int partidosLocal = partidoRepository.contarPartidosPorParejaYCategoria(
                parejaLocalId,
                partido.getCategoria(),
                partido.getFecha().minusMonths(26)
        );

        int partidosVisitante = partidoRepository.contarPartidosPorParejaYCategoria(
                parejaVisitanteId,
                partido.getCategoria(),
                partido.getFecha().minusMonths(26)
        );

        dto.setPartidosJugadosJuntosLocal(partidosLocal);
        dto.setPartidosJugadosJuntosVisitante(partidosVisitante);

        boolean esHabitualLocal = partidosLocal >= 20;
        boolean esHabitualVisitante = partidosVisitante >= 20;

        dto.setParejaLocalEsHabitual(esHabitualLocal);
        dto.setParejaVisitanteEsHabitual(esHabitualVisitante);

        dto.setPuntosParejaHabitualLocal(esHabitualLocal ? pesosComparativaConfig.getPeso(partido.getCategoria(), "parejaHabitual", null) : 
        	partidosLocal >= 10 ? ((int) pesosComparativaConfig.getPeso(partido.getCategoria(), "parejaHabitual", null) / 2) : 0);
        dto.setPuntosParejaHabitualVisitante(esHabitualVisitante ? pesosComparativaConfig.getPeso(partido.getCategoria(), "parejaHabitual", null) : 
        	partidosVisitante >= 10 ? ((int) pesosComparativaConfig.getPeso(partido.getCategoria(), "parejaHabitual", null) / 2) : 0);

        //Se obtienen las estadisticas de cada jugador
        String categoria = partido.getCategoria();
        var stats1 = jugadorEstadisticasRepository.buscarJugador(partido.getJugador1Id(), categoria).orElse(null);
	    var stats2 = jugadorEstadisticasRepository.buscarJugador(partido.getJugador2Id(), categoria).orElse(null);
	    var stats3 = jugadorEstadisticasRepository.buscarJugador(partido.getJugador3Id(), categoria).orElse(null);
	    var stats4 = jugadorEstadisticasRepository.buscarJugador(partido.getJugador4Id(), categoria).orElse(null);

	    int total1 = 0;
	    int total2 = 0;
	    int total3 = 0;
	    int total4 = 0;
	    if (stats1 != null) {
	    	total1 = stats1.getPartidosTotal26m();
	    }
	    if (stats2 != null) {
	    	total2 = stats2.getPartidosTotal26m();
	    }
	    if (stats3 != null) {
	    	total3 = stats3.getPartidosTotal26m();
	    }
	    if (stats4 != null) {
	    	total4 = stats4.getPartidosTotal26m();
	    }
	    
        if (partido.getCategoria().equals(Constantes.ATP) || partido.getCategoria().equals(Constantes.WTA)) {
        	List<Long> ids = Arrays.asList(
            	    partido.getJugador1Id(),
            	    partido.getJugador2Id(),
            	    partido.getJugador3Id(),
            	    partido.getJugador4Id()
            	);
		    List<Jugador> jugadores = jugadorRepository.findAllById(ids);
		    Map<Long, Jugador> jugadoresMap = jugadores.stream()
		    	    .collect(Collectors.toMap(Jugador::getId, j -> j));
	
	    	Jugador jugador1Local = jugadoresMap.get(partido.getJugador1Id());
	    	Jugador jugador2Local = jugadoresMap.get(partido.getJugador2Id());
	    	Jugador jugador1Visitante = jugadoresMap.get(partido.getJugador3Id());
	    	Jugador jugador2Visitante = jugadoresMap.get(partido.getJugador4Id());

		    dto.setRankingJugador1Local(jugador1Local.getRankingDobles());
		    dto.setRankingJugador2Local(jugador2Local.getRankingDobles());
		    dto.setRankingJugador1Visitante(jugador1Visitante.getRankingDobles());
		    dto.setRankingJugador2Visitante(jugador2Visitante.getRankingDobles());
        }

	    dto.setPartidosTotalesJugador1Local(total1);
	    dto.setPartidosTotalesJugador2Local(total2);
	    dto.setPartidosTotalesJugador1Visitante(total3);
	    dto.setPartidosTotalesJugador2Visitante(total4);
	    
        calcularPuntosVictoriasYFormas(dto, partido.getFecha(), partido.getSuperficie(), esHabitualLocal, esHabitualVisitante, partido, stats1, stats2, stats3, stats4);
        calcularHistorialVsRival(dto, parejaLocalId, parejaVisitanteId, partido.getCategoria());

        calcularExperienciaIndividual(dto, partido);
        calcularPuntosRanking(dto);

        dto.setPuntosLocal(
            dto.getPuntosParejaHabitualLocal() +
            dto.getPuntosFormaRecienteLocal() +
            dto.getPuntosVictoriasTotalesLocal() +
            dto.getPuntosSuperficieLocal() +
            dto.getPuntosHistorialVsRivalLocal() +
            dto.getPuntosExperienciaIndividualLocal() + 
            dto.getPuntosRankingLocal()
        );

        dto.setPuntosVisitante(
            dto.getPuntosParejaHabitualVisitante() +
            dto.getPuntosFormaRecienteVisitante() +
            dto.getPuntosVictoriasTotalesVisitante() +
            dto.getPuntosSuperficieVisitante() +
            dto.getPuntosHistorialVsRivalVisitante() +
            dto.getPuntosExperienciaIndividualVisitante() + 
            dto.getPuntosRankingVisitante()
        );
        
        return dto;
    }
	
	private String calcularEstiloComparativa(ProximosPartidosDTO partido) {
	    int puntosLocal = partido.getComparativa().getPuntosLocal();
	    int puntosVisitante = partido.getComparativa().getPuntosVisitante();
	    int diferencia = Math.abs(puntosLocal - puntosVisitante);
	    int minPuntos = Math.min(puntosLocal, puntosVisitante);
	    Double cuota = puntosLocal > puntosVisitante ? partido.getCuotaLocal() : partido.getCuotaVisitante();

	    if (diferencia >= 55) {
    		prediccionPartidoService.guardarPrediccion(partido);
    		return obtenerEstilo(cuota);
	        
	    }else {
	    	if (diferencia >= 50) {
	    		prediccionPartidoService.guardarPrediccion(partido);
	    		return obtenerEstilo(cuota);
		        
		    }else {
		        return "";
		    }
	    }
	}
	
	private void calcularPuntosVictoriasYFormas(ResumenComparativaDTO dto,
	        LocalDate fechaReferencia,
	        String superficie,
	        boolean habitualLocal,
	        boolean habitualVisitante,
	        ProximosPartidosDTO partido, EstadisticasJugador stats1, EstadisticasJugador stats2, EstadisticasJugador stats3, EstadisticasJugador stats4) {
		String categoria = partido.getCategoria();

	    // ------------------ LOCAL ------------------
	    if (habitualLocal) {
	    	Optional<EstadisticasPareja> statsPareja1 = parejaEstadisticasRepository.buscarPareja(partido.getParejaLocalId(), categoria);
	        if (statsPareja1.isPresent()) {
	            cargarEstadisticasPareja(dto, statsPareja1.get(), true, partido.getSuperficie());
	        } else {
	            cargarEstadisticasJugadores(dto, partido.getJugador1Id(), partido.getJugador2Id(), categoria, true, partido.getSuperficie(), stats1, stats2);
	        }

	    } else {
	    	cargarEstadisticasJugadores(dto, partido.getJugador1Id(), partido.getJugador2Id(), categoria, true, partido.getSuperficie(), stats1, stats2);
	    }

	    // ------------------ VISITANTE ------------------
	    if (habitualVisitante) {
	    	Optional<EstadisticasPareja> statsPareja2 = parejaEstadisticasRepository.buscarPareja(partido.getParejaVisitanteId(), categoria);
	        if (statsPareja2.isPresent()) {
	            cargarEstadisticasPareja(dto, statsPareja2.get(), false, partido.getSuperficie());
	        } else {
	            cargarEstadisticasJugadores(dto, partido.getJugador3Id(), partido.getJugador4Id(), categoria, false, partido.getSuperficie(), stats3, stats4);
	        }

	        

	    } else {
	    	cargarEstadisticasJugadores(dto, partido.getJugador3Id(), partido.getJugador4Id(), categoria, false, partido.getSuperficie(), stats3, stats4);
	    }
	    
	    dto.setPuntosVictoriasTotalesLocal((int) calculoPuntos(dto.getPorcentajeTotalLocal(), pesosComparativaConfig.getPeso(partido.getCategoria(), "total", dto.getPartidosTotalesJugador1Local() + dto.getPartidosTotalesJugador2Local())));
        dto.setPuntosSuperficieLocal((int) calculoPuntos(dto.getPorcentajeSuperficieLocal(), pesosComparativaConfig.getPeso(partido.getCategoria(), "superficie", dto.getPartidosTotalesJugador1Local() + dto.getPartidosTotalesJugador2Local())));
        dto.setPuntosFormaRecienteLocal((int) calculoPuntos(dto.getPorcentajeFormaRecienteLocal(), pesosComparativaConfig.getPeso(partido.getCategoria(), "forma", dto.getPartidosTotalesJugador1Local() + dto.getPartidosTotalesJugador2Local())));
        
        dto.setPuntosVictoriasTotalesVisitante((int) calculoPuntos(dto.getPorcentajeTotalVisitante(), pesosComparativaConfig.getPeso(partido.getCategoria(), "total", dto.getPartidosTotalesJugador1Visitante() + dto.getPartidosTotalesJugador2Visitante())));
        dto.setPuntosSuperficieVisitante((int) calculoPuntos(dto.getPorcentajeSuperficieVisitante(), pesosComparativaConfig.getPeso(partido.getCategoria(), "superficie", dto.getPartidosTotalesJugador1Visitante() + dto.getPartidosTotalesJugador2Visitante())));
        dto.setPuntosFormaRecienteVisitante((int) calculoPuntos(dto.getPorcentajeFormaRecienteVisitante(), pesosComparativaConfig.getPeso(partido.getCategoria(), "forma", dto.getPartidosTotalesJugador1Visitante() + dto.getPartidosTotalesJugador2Visitante())));

    }
	
	private void calcularHistorialVsRival(ResumenComparativaDTO dto, Long localId, Long visitanteId, String categoria) {
	    int ganadosLocal = partidoRepository.countGanadosEntreParejas(localId, visitanteId);
	    int ganadosVisitante = partidoRepository.countGanadosEntreParejas(visitanteId, localId);

	    dto.setPartidosGanadosContraRivalLocal(ganadosLocal);
	    dto.setPartidosGanadosContraRivalVisitante(ganadosVisitante);

	    int diff = ganadosLocal - ganadosVisitante;

	    if (diff > 0) {
	        dto.setPuntosHistorialVsRivalLocal(pesosComparativaConfig.getPeso(categoria, "h2h", null));
	        dto.setPuntosHistorialVsRivalVisitante(0);
	    } else if (diff < 0) {
	        dto.setPuntosHistorialVsRivalVisitante(pesosComparativaConfig.getPeso(categoria, "h2h", null));
	        dto.setPuntosHistorialVsRivalLocal(0);
	    }
	}
	
	private void calcularExperienciaIndividual(ResumenComparativaDTO dto, ProximosPartidosDTO partido) {
	    int total1 = dto.getPartidosTotalesJugador1Local();
	    int total2 = dto.getPartidosTotalesJugador2Local();
	    int total3 = dto.getPartidosTotalesJugador1Visitante();
	    int total4 = dto.getPartidosTotalesJugador2Visitante();

	    dto.setPuntosExperienciaIndividualLocal(
	    		(total1 + total2) > 200 
	    	        ? pesosComparativaConfig.getPeso(partido.getCategoria(), "experiencia", null) 
	    	        : (int) Math.round((double) (total1 + total2) * pesosComparativaConfig.getPeso(partido.getCategoria(), "experiencia", null) / 200)
	    	);
	    dto.setPuntosExperienciaIndividualVisitante(
	    		(total3 + total4) > 200 
	    	        ? pesosComparativaConfig.getPeso(partido.getCategoria(), "experiencia", null) 
	    	        : (int) Math.round((double) (total3 + total4) * pesosComparativaConfig.getPeso(partido.getCategoria(), "experiencia", null) / 200)
	    	);
	}
	
	private void calcularPuntosRanking(ResumenComparativaDTO dto) {
    	int puntosLocal = 0;
    	int puntosVisitante = 0;
		if (dto.getRankingJugador1Local() != null) {
			if (dto.getRankingJugador1Local() <= 15) {
				puntosLocal = puntosLocal + 5;
			} else {
				puntosLocal = puntosLocal + 2;
			}
		}
		if (dto.getRankingJugador2Local() != null) {
			if (dto.getRankingJugador2Local() <= 15) {
				puntosLocal = puntosLocal + 5;
			} else {
				puntosLocal = puntosLocal + 2;
			}
		}
		if (dto.getRankingJugador1Visitante() != null) {
			if (dto.getRankingJugador1Visitante() <= 15) {
				puntosVisitante = puntosVisitante + 5;
			} else {
				puntosVisitante = puntosVisitante + 2;
			}
		}
		if (dto.getRankingJugador2Visitante() != null) {
			if (dto.getRankingJugador2Visitante() <= 15) {
				puntosVisitante = puntosVisitante + 5;
			} else {
				puntosVisitante = puntosVisitante + 2;
			}
		}
		dto.setPuntosRankingLocal(puntosLocal);
		dto.setPuntosRankingVisitante(puntosVisitante);
	}
	
	private String obtenerEstilo(Double cuota) {
		if (cuota == null) {
			return "resaltado-neutro";
		}
		if (cuota < 1.2) {
			return "resaltado-medio";
		}
		return "resaltado-fuerte";
	}
	
	private void cargarEstadisticasPareja(ResumenComparativaDTO dto, EstadisticasPareja stats, boolean esPareja1, String superficie) {
	    if (esPareja1) {
	        dto.setPorcentajeTotalLocal(stats.getPorcentajeTotal26m());
	        dto.setPorcentajeFormaRecienteLocal(stats.getPorcentajeTotal4m());
	        if (superficie != null) {
		        if (superficie.toUpperCase().contains("GRASS")) {
		        	dto.setPorcentajeSuperficieLocal(stats.getPorcentajeHierba26m());
		        } else {
		        	if (superficie.toUpperCase().contains("CLAY")) {
		        		dto.setPorcentajeSuperficieLocal(stats.getPorcentajeTierra26m());
			        } else {
			        	dto.setPorcentajeSuperficieLocal(stats.getPorcentajeDura26m());
			        }
		        }
	        } else {
	        	dto.setPorcentajeSuperficieLocal(0);
	        }
	    } else {
	    	dto.setPorcentajeTotalVisitante(stats.getPorcentajeTotal26m());
	        dto.setPorcentajeFormaRecienteVisitante(stats.getPorcentajeTotal4m());
	        if (superficie != null) {
		        if (superficie.toUpperCase().contains("GRASS")) {
		        	dto.setPorcentajeSuperficieVisitante(stats.getPorcentajeHierba26m());
		        } else {
		        	if (superficie.toUpperCase().contains("CLAY")) {
		        		dto.setPorcentajeSuperficieVisitante(stats.getPorcentajeTierra26m());
			        } else {
			        	dto.setPorcentajeSuperficieVisitante(stats.getPorcentajeDura26m());
			        }
		        }
	        } else {
	        	dto.setPorcentajeSuperficieVisitante(0);
	        }
	    }
	}

	private void cargarEstadisticasJugadores(ResumenComparativaDTO dto, Long jugadorId1, Long jugadorId2, String categoria, boolean esPareja1, 
			String superficie, EstadisticasJugador stats1, EstadisticasJugador stats2) {
	    
	    if (stats1 == null) {
	    	stats1 = new EstadisticasJugador();
	    	stats1.setPorcentajeTotal26m(0.0);
	    	stats1.setPorcentajeTotal4m(0.0);
	    	stats1.setPorcentajeTierra26m(0.0);
	    	stats1.setPorcentajeHierba26m(0.0);
	    	stats1.setPorcentajeDura26m(0.0);
	    }
	    
	    if (stats2 == null) {
	    	stats2 = new EstadisticasJugador();
	    	stats2.setPorcentajeTotal26m(0.0);
	    	stats2.setPorcentajeTotal4m(0.0);
	    	stats2.setPorcentajeTierra26m(0.0);
	    	stats2.setPorcentajeHierba26m(0.0);
	    	stats2.setPorcentajeDura26m(0.0);
	    }

	    double pctTotal26m = (stats1.getPorcentajeTotal26m() + stats2.getPorcentajeTotal26m()) / 2.0;
	    double pctUlt4m    = (stats1.getPorcentajeTotal4m() + stats2.getPorcentajeTotal4m()) / 2.0;
	    double pctTierra   = (stats1.getPorcentajeTierra26m() + stats2.getPorcentajeTierra26m()) / 2.0;
	    double pctHierba   = (stats1.getPorcentajeHierba26m() + stats2.getPorcentajeHierba26m()) / 2.0;
	    double pctDura     = (stats1.getPorcentajeDura26m() + stats2.getPorcentajeDura26m()) / 2.0;

	    if (esPareja1) {
	    	dto.setPorcentajeTotalLocal(pctTotal26m);
	        dto.setPorcentajeFormaRecienteLocal(pctUlt4m);
	        if (superficie != null) {
		        if (superficie.toUpperCase().contains("GRASS")) {
		        	dto.setPorcentajeSuperficieLocal(pctHierba);
		        } else {
		        	if (superficie.toUpperCase().contains("CLAY")) {
		        		dto.setPorcentajeSuperficieLocal(pctTierra);
			        } else {
			        	dto.setPorcentajeSuperficieLocal(pctDura);
			        }
		        }
	        } else {
	        	dto.setPorcentajeSuperficieLocal(0);
	        }
	    } else {
	    	dto.setPorcentajeTotalVisitante(pctTotal26m);
	        dto.setPorcentajeFormaRecienteVisitante(pctUlt4m);
	        if (superficie != null) {
		        if (superficie.toUpperCase().contains("GRASS")) {
		        	dto.setPorcentajeSuperficieVisitante(pctHierba);
		        } else {
		        	if (superficie.toUpperCase().contains("CLAY")) {
		        		dto.setPorcentajeSuperficieVisitante(pctTierra);
			        } else {
			        	dto.setPorcentajeSuperficieVisitante(pctDura);
			        }
		        }
	        } else {
	        	dto.setPorcentajeSuperficieVisitante(0);
	        }
	    }
	}
	
	private int calculoPuntos(double porcentajeVictorias, int puntuacionMaxima) {
		if (porcentajeVictorias < 25) {
			return 0;
		} else {
			if (porcentajeVictorias > 80) {
				return puntuacionMaxima;
			} else {
				if (porcentajeVictorias < 34) {
					return (int) (puntuacionMaxima * 0.1);
				} else {
					if (porcentajeVictorias < 40) {
						return (int) (puntuacionMaxima * 0.2);
					}
				}
			}
		}
		return (int) ((porcentajeVictorias - 40) * (puntuacionMaxima - (int) (puntuacionMaxima * 0.2)) / 40) + (int) (puntuacionMaxima * 0.2);
	}
	
	private boolean contieneJugadorFavorito(ProximosPartidosDTO partido,  Set<Long> idsJugadoresFavoritos) {
        

        // Verificar si alguno de los 4 jugadores del partido está en los favoritos
        return idsJugadoresFavoritos.contains(partido.getJugador1Id()) ||
               idsJugadoresFavoritos.contains(partido.getJugador2Id()) ||
               idsJugadoresFavoritos.contains(partido.getJugador3Id()) ||
               idsJugadoresFavoritos.contains(partido.getJugador4Id());
    }

}
