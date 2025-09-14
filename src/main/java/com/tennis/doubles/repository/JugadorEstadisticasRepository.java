package com.tennis.doubles.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.tennis.doubles.model.EstadisticasJugador;

import java.util.Optional;

public interface JugadorEstadisticasRepository extends JpaRepository<EstadisticasJugador, Long> {
	
	@Query("SELECT j FROM EstadisticasJugador j WHERE j.id.jugadorId = :jugadorId AND j.id.categoria = :categoria")
    Optional<EstadisticasJugador> buscarJugador(@Param("jugadorId") Long jugadorId, @Param("categoria") String categoria);
	
	
	@Modifying
    @Transactional
    @Query(
        value = """
        INSERT INTO estadisticas_jugador (
    jugador_id, categoria,
    pct_total_26m, pct_total_4m, pct_tierra_26m, pct_hierba_26m, pct_dura_26m,
    partidos_total_26m, partidos_total_4m, partidos_tierra_26m, partidos_hierba_26m, partidos_dura_26m
)
SELECT
    t.jugador_id,
    t.categoria_group,

    COALESCE(ROUND(
      100.0 * SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) THEN t.won ELSE 0 END)
      / NULLIF(SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) THEN 1 ELSE 0 END), 0)
    , 2), 0) AS pct_total_26m,

    COALESCE(ROUND(
      100.0 * SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 4 MONTH) THEN t.won ELSE 0 END)
      / NULLIF(SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 4 MONTH) THEN 1 ELSE 0 END), 0)
    , 2), 0) AS pct_total_4m,

    COALESCE(ROUND(
      100.0 * SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) AND t.superficie = 'CLAY' THEN t.won ELSE 0 END)
      / NULLIF(SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) AND t.superficie = 'CLAY' THEN 1 ELSE 0 END), 0)
    , 2), 0) AS pct_tierra_26m,

    COALESCE(ROUND(
      100.0 * SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) AND t.superficie = 'GRASS' THEN t.won ELSE 0 END)
      / NULLIF(SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) AND t.superficie = 'GRASS' THEN 1 ELSE 0 END), 0)
    , 2), 0) AS pct_hierba_26m,

    COALESCE(ROUND(
      100.0 * SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) AND t.superficie = 'HARD' THEN t.won ELSE 0 END)
      / NULLIF(SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) AND t.superficie = 'HARD' THEN 1 ELSE 0 END), 0)
    , 2), 0) AS pct_dura_26m,

    SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) THEN 1 ELSE 0 END) AS partidos_total_26m,
    SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 4 MONTH) THEN 1 ELSE 0 END) AS partidos_total_4m,
    SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) AND t.superficie = 'CLAY' THEN 1 ELSE 0 END) AS partidos_tierra_26m,
    SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) AND t.superficie = 'GRASS' THEN 1 ELSE 0 END) AS partidos_hierba_26m,
    SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) AND t.superficie = 'HARD' THEN 1 ELSE 0 END) AS partidos_dura_26m

FROM (
    -- Jugador1 como ganador
    SELECT pa.jugador1_id AS jugador_id,
			tt.categoria AS categoria_group,
           CASE WHEN UPPER(tt.superficie) LIKE '%CLAY%' THEN 'CLAY'
                WHEN UPPER(tt.superficie) LIKE '%GRASS%' THEN 'GRASS'
                ELSE 'HARD' END AS superficie,
           p.fecha,
           1 AS won
    FROM partido p
    JOIN pareja pa ON p.pareja_ganadora_id = pa.id
    JOIN torneo tt ON p.torneo_id = tt.id

    UNION ALL
    -- Jugador2 como ganador
    SELECT pa.jugador2_id, 
			tt.categoria,
           CASE WHEN UPPER(tt.superficie) LIKE '%CLAY%' THEN 'CLAY'
                WHEN UPPER(tt.superficie) LIKE '%GRASS%' THEN 'GRASS'
                ELSE 'HARD' END,
           p.fecha,
           1
    FROM partido p
    JOIN pareja pa ON p.pareja_ganadora_id = pa.id
    JOIN torneo tt ON p.torneo_id = tt.id

    UNION ALL
    -- Jugador1 como perdedor
    SELECT pa.jugador1_id,
			tt.categoria,
           CASE WHEN UPPER(tt.superficie) LIKE '%CLAY%' THEN 'CLAY'
                WHEN UPPER(tt.superficie) LIKE '%GRASS%' THEN 'GRASS'
                ELSE 'HARD' END,
           p.fecha,
           0
    FROM partido p
    JOIN pareja pa ON p.pareja_perdedora_id = pa.id
    JOIN torneo tt ON p.torneo_id = tt.id

    UNION ALL
    -- Jugador2 como perdedor
    SELECT pa.jugador2_id,
			tt.categoria,
           CASE WHEN UPPER(tt.superficie) LIKE '%CLAY%' THEN 'CLAY'
                WHEN UPPER(tt.superficie) LIKE '%GRASS%' THEN 'GRASS'
                ELSE 'HARD' END,
           p.fecha,
           0
    FROM partido p
    JOIN pareja pa ON p.pareja_perdedora_id = pa.id
    JOIN torneo tt ON p.torneo_id = tt.id
) AS t

GROUP BY t.jugador_id, t.categoria_group
        """,
        nativeQuery = true
    )
    void insertEstadisticasJugadores();
	
	@Modifying
    @Transactional
    @Query("DELETE FROM EstadisticasJugador")
    void borrarEstadisticasJugadores();
}