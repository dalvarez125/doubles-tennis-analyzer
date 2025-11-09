package com.tennis.doubles.repository;

import com.tennis.doubles.dto.detalle.DetalleParejaDTO;
import com.tennis.doubles.dto.rankings.RankingParejaDTO;
import com.tennis.doubles.model.Pareja;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParejaRepository extends JpaRepository<Pareja, Long> {
	
	List<Pareja> findByFavorito(Boolean favorito);
	
	@Query(value = """
		    SELECT 
		        pa.id AS id,
		        j1.nombre AS jugador1,
		        j2.nombre AS jugador2,
		        SUM(victorias) AS victorias,
		        SUM(derrotas) AS derrotas,
		        SUM(total_partidos) AS total_partidos,
		        ROUND(100.0 * SUM(victorias) / SUM(total_partidos), 2) AS porcentaje_victorias,
		        pa.favorito
		    FROM pareja pa
		    JOIN jugador j1 ON pa.jugador1_id = j1.id
		    JOIN jugador j2 ON pa.jugador2_id = j2.id
		    JOIN (
		        SELECT p.pareja_ganadora_id AS pareja_id, 1 AS victorias, 0 AS derrotas, 1 AS total_partidos, p.torneo_id, p.fecha
		        FROM partido p
		        UNION ALL
		        SELECT p.pareja_perdedora_id AS pareja_id, 0 AS victorias, 1 AS derrotas, 1 AS total_partidos, p.torneo_id, p.fecha
		        FROM partido p
		    ) AS p_stats ON p_stats.pareja_id = pa.id
		    JOIN torneo t ON p_stats.torneo_id = t.id
		    WHERE t.categoria = :categoria
		      AND (:superficie IS NULL OR LOWER(t.superficie) LIKE CONCAT('%', LOWER(:superficie), '%'))
		      AND (:fechaInicio IS NULL OR :fechaFin IS NULL OR p_stats.fecha BETWEEN :fechaInicio AND :fechaFin)
		      AND (pa.favorito = true OR pa.favorito = :favorito)
		    GROUP BY pa.id, j1.nombre, j2.nombre, pa.favorito
		    HAVING SUM(total_partidos) >= :minPartidos
		    ORDER BY porcentaje_victorias DESC, total_partidos DESC
		""", nativeQuery = true)
		List<RankingParejaDTO> obtenerRankingParejas(
		    @Param("categoria") String categoria,
		    @Param("superficie") String superficie,
		    @Param("fechaInicio") LocalDate fechaInicio,
		    @Param("fechaFin") LocalDate fechaFin,
		    @Param("minPartidos") int minPartidos, 
		    @Param("favorito") Boolean favorito
		);
	
	
	@Query(value = """
		    SELECT 
		        p.fecha,
		        CONCAT(jr1.nombre, ' / ', jr2.nombre) AS rivales,
		        p.marcador,
		        CASE 
		            WHEN p.pareja_ganadora_id = :parejaId THEN 'Victoria'
		            ELSE 'Derrota'
		        END AS resultado,
		        t.nombre AS torneo,
		        t.categoria,
		        t.superficie,
		        p.id AS partido_id,
			    CONCAT(jp1.nombre, ' / ', jp2.nombre) AS pareja,
			    p.ronda
		    FROM partido p
		    JOIN pareja pa ON (p.pareja_ganadora_id = pa.id OR p.pareja_perdedora_id = pa.id)
		    JOIN pareja rivales ON (
		        (p.pareja_ganadora_id = :parejaId AND p.pareja_perdedora_id = rivales.id) OR
		        (p.pareja_perdedora_id = :parejaId AND p.pareja_ganadora_id = rivales.id)
		    )
		    JOIN jugador jr1 ON rivales.jugador1_id = jr1.id
		    JOIN jugador jr2 ON rivales.jugador2_id = jr2.id
		    JOIN jugador jp1 ON pa.jugador1_id = jp1.id
			JOIN jugador jp2 ON pa.jugador2_id = jp2.id
		    JOIN torneo t ON p.torneo_id = t.id
		    WHERE pa.id = :parejaId
		    AND (:fechaInicio IS NULL OR :fechaFin IS NULL OR p.fecha BETWEEN :fechaInicio AND :fechaFin)
		    AND (:superficie IS NULL OR LOWER(t.superficie) LIKE CONCAT('%', LOWER(:superficie), '%'))
		    ORDER BY p.fecha DESC
		    """, nativeQuery = true)
		List<DetalleParejaDTO> obtenerDetallePareja(
		    @Param("parejaId") Long parejaId,
		    @Param("fechaInicio") LocalDate fechaInicio,
		    @Param("fechaFin") LocalDate fechaFin,
		    @Param("superficie") String superficie
		);
	
}
