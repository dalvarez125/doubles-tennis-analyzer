package com.tennis.doubles.repository;

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
}
