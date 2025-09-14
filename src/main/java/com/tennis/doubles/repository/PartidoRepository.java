package com.tennis.doubles.repository;

import com.tennis.doubles.dto.carga.partidos.JugadorEstadisticasDTO;
import com.tennis.doubles.dto.carga.partidos.ParejaEstadisticasDTO;
import com.tennis.doubles.model.Partido;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartidoRepository extends JpaRepository<Partido, Long> {
	
	@Query("SELECT COUNT(p) " +
	           "FROM Partido p " +
	           "WHERE (p.parejaGanadora.id = :parejaId OR p.parejaPerdedora.id = :parejaId) " +
	           "AND p.torneo.categoria = :categoria " +
	           "AND p.fecha >= :fechaDesde")
	    int contarPartidosPorParejaYCategoria(@Param("parejaId") Long parejaId,
	                                          @Param("categoria") String categoria,
	                                          @Param("fechaDesde") LocalDate fechaDesde);

    @Query("SELECT COUNT(p) FROM Partido p WHERE p.parejaGanadora.id = :ganadora AND p.parejaPerdedora.id = :perdedora")
    int countGanadosEntreParejas(@Param("ganadora") Long ganadora, @Param("perdedora") Long perdedora);
    	
    	// ---- PAREJA ----
        @Query("""
            SELECT NEW com.tennis.doubles.dto.carga.partidos.ParejaEstadisticasDTO(

                (COUNT(CASE WHEN p.parejaGanadora.id = :parejaId AND p.fecha >= :fecha26m THEN 1 END) * 100.0 /
                 NULLIF(COUNT(CASE WHEN p.fecha >= :fecha26m THEN 1 END), 0)),


                (COUNT(CASE WHEN p.parejaGanadora.id = :parejaId AND p.torneo.superficie = :superficie AND p.fecha >= :fecha26m THEN 1 END) * 100.0 /
                 NULLIF(COUNT(CASE WHEN p.torneo.superficie = :superficie AND p.fecha >= :fecha26m THEN 1 END), 0)),


                (COUNT(CASE WHEN p.parejaGanadora.id = :parejaId AND p.fecha >= :fecha4m THEN 1 END) * 100.0 /
                 NULLIF(COUNT(CASE WHEN p.fecha >= :fecha4m THEN 1 END), 0))
            )
            FROM Partido p
            WHERE (p.parejaGanadora.id = :parejaId OR p.parejaPerdedora.id = :parejaId)
              AND p.torneo.categoria = :categoria
        """)
        ParejaEstadisticasDTO obtenerEstadisticasPareja(
                @Param("parejaId") Long parejaId,
                @Param("categoria") String categoria,
                @Param("superficie") String superficie,
                @Param("fecha26m") LocalDate fecha26m,
                @Param("fecha4m") LocalDate fecha4m
        );


        // ---- JUGADOR ----
        @Query("""
            SELECT NEW com.tennis.doubles.dto.carga.partidos.JugadorEstadisticasDTO(

                (COUNT(CASE WHEN p.parejaGanadora.jugador1.id = :jugadorId OR p.parejaGanadora.jugador2.id = :jugadorId AND p.fecha >= :fecha26m THEN 1 END) * 100.0 /
                 NULLIF(COUNT(CASE WHEN (p.parejaGanadora.jugador1.id = :jugadorId OR p.parejaGanadora.jugador2.id = :jugadorId OR
                                         p.parejaPerdedora.jugador1.id = :jugadorId OR p.parejaPerdedora.jugador2.id = :jugadorId)
                                   AND p.fecha >= :fecha26m THEN 1 END), 0)),


                (COUNT(CASE WHEN (p.parejaGanadora.jugador1.id = :jugadorId OR p.parejaGanadora.jugador2.id = :jugadorId)
                                  AND p.torneo.superficie = :superficie AND p.fecha >= :fecha26m THEN 1 END) * 100.0 /
                 NULLIF(COUNT(CASE WHEN (p.parejaGanadora.jugador1.id = :jugadorId OR p.parejaGanadora.jugador2.id = :jugadorId OR
                                         p.parejaPerdedora.jugador1.id = :jugadorId OR p.parejaPerdedora.jugador2.id = :jugadorId)
                                   AND p.torneo.superficie = :superficie AND p.fecha >= :fecha26m THEN 1 END), 0)),


                (COUNT(CASE WHEN (p.parejaGanadora.jugador1.id = :jugadorId OR p.parejaGanadora.jugador2.id = :jugadorId)
                                  AND p.fecha >= :fecha3m THEN 1 END) * 100.0 /
                 NULLIF(COUNT(CASE WHEN (p.parejaGanadora.jugador1.id = :jugadorId OR p.parejaGanadora.jugador2.id = :jugadorId OR
                                         p.parejaPerdedora.jugador1.id = :jugadorId OR p.parejaPerdedora.jugador2.id = :jugadorId)
                                   AND p.fecha >= :fecha3m THEN 1 END), 0))
            )
            FROM Partido p
            WHERE (p.parejaGanadora.jugador1.id = :jugadorId OR p.parejaGanadora.jugador2.id = :jugadorId
                OR p.parejaPerdedora.jugador1.id = :jugadorId OR p.parejaPerdedora.jugador2.id = :jugadorId)
              AND p.torneo.categoria = :categoria
        """)
        JugadorEstadisticasDTO obtenerEstadisticasJugador(
                @Param("jugadorId") Long jugadorId,
                @Param("categoria") String categoria,
                @Param("superficie") String superficie,
                @Param("fecha26m") LocalDate fecha26m,
                @Param("fecha3m") LocalDate fecha3m
        );
}
