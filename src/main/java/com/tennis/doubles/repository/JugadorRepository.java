package com.tennis.doubles.repository;

import com.tennis.doubles.dto.detalle.DetalleJugadorDTO;
import com.tennis.doubles.dto.detalle.DetalleParejaDTO;
import com.tennis.doubles.dto.rankings.RankingJugadorDTO;
import com.tennis.doubles.model.Jugador;

import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {
	
	Optional<Jugador> findByNombre(String nombre);
	List<Jugador> findByRankingDoblesNotNull();
	
	@Modifying
    @Transactional
    @Query("UPDATE Jugador j SET j.rankingDobles = null")
    void resetearRankingDobles();
	
	@Query("SELECT j FROM Jugador j WHERE LOWER(j.nombre) LIKE LOWER(CONCAT(:inicial, '%')) AND LOWER(j.nombre) LIKE LOWER(CONCAT('%', :apellido, '%')) AND j.categoria = 'ATP'")
	List<Jugador> buscarPorInicialYApellido(@Param("inicial") String inicial, @Param("apellido") String apellido);
	
	@Query(value = """
		    SELECT 
		        j.id AS jugador_id,
		        j.nombre AS nombre_jugador,

		        SUM(CASE WHEN p.pareja_ganadora_id = pa.id THEN 1 ELSE 0 END) AS victorias,
		        SUM(CASE WHEN p.pareja_perdedora_id = pa.id THEN 1 ELSE 0 END) AS derrotas,

		        COUNT(*) AS total_partidos,

		        ROUND(
		            100 * SUM(CASE WHEN p.pareja_ganadora_id = pa.id THEN 1 ELSE 0 END) /
		            NULLIF(
		                SUM(CASE 
		                    WHEN p.pareja_ganadora_id = pa.id THEN 1
		                    WHEN p.pareja_perdedora_id = pa.id THEN 1
		                    ELSE 0
		                END), 0
		            ),
		            2
		        ) AS porcentaje_victorias

		    FROM jugador j
		    JOIN pareja pa ON j.id = pa.jugador1_id OR j.id = pa.jugador2_id
		    JOIN partido p ON p.pareja_ganadora_id = pa.id OR p.pareja_perdedora_id = pa.id
		    JOIN torneo t ON p.torneo_id = t.id

		    WHERE t.categoria = 'ATP/WTA'
		      AND j.categoria = :categoria
		      AND (:superficie IS NULL OR LOWER(t.superficie) LIKE CONCAT('%', LOWER(:superficie), '%'))
		      AND (:fechaInicio IS NULL OR :fechaFin IS NULL OR p.fecha BETWEEN :fechaInicio AND :fechaFin)

		    GROUP BY j.id, j.nombre

		    HAVING (SUM(CASE 
		        WHEN p.pareja_ganadora_id = pa.id THEN 1 
		        WHEN p.pareja_perdedora_id = pa.id THEN 1 
		        ELSE 0 
		    END)) >= :minPartidos

		    ORDER BY porcentaje_victorias DESC, total_partidos DESC
		""", nativeQuery = true)
		List<RankingJugadorDTO> obtenerRankingJugadores(
		    @Param("categoria") String categoria,
		    @Param("superficie") String superficie,
		    @Param("fechaInicio") LocalDate fechaInicio,
		    @Param("fechaFin") LocalDate fechaFin,
		    @Param("minPartidos") int minPartidos
		);
	
	@Query(value = """
		    SELECT 
		        j.id AS jugador_id,
		        j.nombre AS nombre_jugador,

		        SUM(CASE WHEN p.pareja_ganadora_id = pa.id THEN 1 ELSE 0 END) AS victorias,
		        SUM(CASE WHEN p.pareja_perdedora_id = pa.id THEN 1 ELSE 0 END) AS derrotas,

		        COUNT(*) AS total_partidos,

		        ROUND(
		            100 * SUM(CASE WHEN p.pareja_ganadora_id = pa.id THEN 1 ELSE 0 END) /
		            NULLIF(
		                SUM(CASE 
		                    WHEN p.pareja_ganadora_id = pa.id THEN 1
		                    WHEN p.pareja_perdedora_id = pa.id THEN 1
		                    ELSE 0
		                END), 0
		            ),
		            2
		        ) AS porcentaje_victorias

		    FROM jugador j
		    JOIN pareja pa ON j.id = pa.jugador1_id OR j.id = pa.jugador2_id
		    JOIN partido p ON p.pareja_ganadora_id = pa.id OR p.pareja_perdedora_id = pa.id
		    JOIN torneo t ON p.torneo_id = t.id

		    WHERE t.categoria = :categoria
		      AND (:superficie IS NULL OR LOWER(t.superficie) LIKE CONCAT('%', LOWER(:superficie), '%'))
		      AND (:fechaInicio IS NULL OR :fechaFin IS NULL OR p.fecha BETWEEN :fechaInicio AND :fechaFin)

		    GROUP BY j.id, j.nombre

		    HAVING (SUM(CASE 
		        WHEN p.pareja_ganadora_id = pa.id THEN 1 
		        WHEN p.pareja_perdedora_id = pa.id THEN 1 
		        ELSE 0 
		    END)) >= :minPartidos

		    ORDER BY porcentaje_victorias DESC, total_partidos DESC
		""", nativeQuery = true)
		List<RankingJugadorDTO> obtenerRankingJugadoresITF(
		    @Param("categoria") String categoria,
		    @Param("superficie") String superficie,
		    @Param("fechaInicio") LocalDate fechaInicio,
		    @Param("fechaFin") LocalDate fechaFin,
		    @Param("minPartidos") int minPartidos
		);
	
	@Query("""
	        SELECT CASE WHEN COUNT(j) > 0 THEN true ELSE false END
	        FROM Jugador j
	        WHERE j.id = :jugadorId
	          AND (LOWER(j.categoria) LIKE '%wta%' OR LOWER(j.categoria) LIKE '%women%')
	        """)
	    boolean isFemenino(@Param("jugadorId") Long jugadorId);
	
	
	@Query(value = """
		    SELECT *
		    FROM (
		      SELECT 
		        p.fecha,
		        CONCAT(pr1.nombre, ' / ', pr2.nombre) AS rivales,
		        p.marcador,
		        'Victoria' AS resultado,
		        t.nombre AS torneo,
		        t.superficie,
		        p.id AS partido_id,
		        CASE WHEN jp1.id = :jugadorId THEN jp1.nombre ELSE jp2.nombre END AS jugador,
		        CASE WHEN jp1.id = :jugadorId THEN jp2.nombre ELSE jp1.nombre END AS pareja,
		        p.ronda
		      FROM partido p
		      JOIN pareja pg ON p.pareja_ganadora_id = pg.id
		      JOIN pareja pp ON p.pareja_perdedora_id = pp.id
		      JOIN jugador jp1 ON pg.jugador1_id = jp1.id
		      JOIN jugador jp2 ON pg.jugador2_id = jp2.id
		      JOIN jugador pr1 ON pp.jugador1_id = pr1.id
		      JOIN jugador pr2 ON pp.jugador2_id = pr2.id
		      JOIN torneo t ON p.torneo_id = t.id
		      WHERE (pg.jugador1_id = :jugadorId OR pg.jugador2_id = :jugadorId)
		        AND (:fechaInicio IS NULL OR :fechaFin IS NULL OR p.fecha BETWEEN :fechaInicio AND :fechaFin)
		        AND (:superficie IS NULL OR LOWER(t.superficie) LIKE CONCAT('%', LOWER(:superficie), '%'))

		      UNION

		      SELECT 
		        p.fecha,
		        CONCAT(pg1.nombre, ' / ', pg2.nombre) AS rivales,
		        p.marcador,
		        'Derrota' AS resultado,
		        t.nombre AS torneo,
		        t.superficie,
		        p.id AS partido_id,
		        CASE WHEN pp1.id = :jugadorId THEN pp1.nombre ELSE pp2.nombre END AS jugador,
		        CASE WHEN pp1.id = :jugadorId THEN pp2.nombre ELSE pp1.nombre END AS pareja,
		        p.ronda
		      FROM partido p
		      JOIN pareja pg ON p.pareja_ganadora_id = pg.id
		      JOIN pareja pp ON p.pareja_perdedora_id = pp.id
		      JOIN jugador pg1 ON pg.jugador1_id = pg1.id
		      JOIN jugador pg2 ON pg.jugador2_id = pg2.id
		      JOIN jugador pp1 ON pp.jugador1_id = pp1.id
		      JOIN jugador pp2 ON pp.jugador2_id = pp2.id
		      JOIN torneo t ON p.torneo_id = t.id
		      WHERE (pp.jugador1_id = :jugadorId OR pp.jugador2_id = :jugadorId)
		        AND (:fechaInicio IS NULL OR :fechaFin IS NULL OR p.fecha BETWEEN :fechaInicio AND :fechaFin)
		        AND (:superficie IS NULL OR LOWER(t.superficie) LIKE CONCAT('%', LOWER(:superficie), '%'))
		    ) AS sub
		    ORDER BY sub.fecha DESC
		    """, nativeQuery = true)
	List<DetalleJugadorDTO> obtenerDetalleJugador(
		    @Param("jugadorId") Long jugadorId,
		    @Param("fechaInicio") LocalDate fechaInicio,
		    @Param("fechaFin") LocalDate fechaFin,
		    @Param("superficie") String superficie
		);

}
