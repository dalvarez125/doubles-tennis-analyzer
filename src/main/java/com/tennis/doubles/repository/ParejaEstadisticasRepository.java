package com.tennis.doubles.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.tennis.doubles.model.EstadisticasPareja;

import java.util.Optional;

public interface ParejaEstadisticasRepository extends JpaRepository<EstadisticasPareja, Long> {
	
	@Query("SELECT j FROM EstadisticasPareja j WHERE j.id.parejaId = :parejaId AND j.id.categoria = :categoria")
    Optional<EstadisticasPareja> buscarPareja(@Param("parejaId") Long parejaId, @Param("categoria") String categoria);
	
	@Modifying
    @Transactional
    @Query(
        value = """
        INSERT INTO estadisticas_pareja (
    pareja_id, categoria,
    pct_total_26m, pct_total_4m, pct_tierra_26m, pct_hierba_26m, pct_dura_26m,
    partidos_total_26m, partidos_total_4m, partidos_tierra_26m, partidos_hierba_26m, partidos_dura_26m
)
SELECT
    t.pareja_id,
    t.categoria,

    COALESCE(ROUND(
      100.0 * SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) THEN t.won ELSE 0 END)
      / NULLIF(SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) THEN 1 ELSE 0 END), 0), 2), 0) AS pct_total_26m,

    COALESCE(ROUND(
      100.0 * SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 4 MONTH) THEN t.won ELSE 0 END)
      / NULLIF(SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 4 MONTH) THEN 1 ELSE 0 END), 0), 2), 0) AS pct_total_4m,

    COALESCE(ROUND(
      100.0 * SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) AND t.superficie = 'CLAY' THEN t.won ELSE 0 END)
      / NULLIF(SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) AND t.superficie = 'CLAY' THEN 1 ELSE 0 END), 0), 2), 0) AS pct_tierra_26m,

    COALESCE(ROUND(
      100.0 * SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) AND t.superficie = 'GRASS' THEN t.won ELSE 0 END)
      / NULLIF(SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) AND t.superficie = 'GRASS' THEN 1 ELSE 0 END), 0), 2), 0) AS pct_hierba_26m,

    COALESCE(ROUND(
      100.0 * SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) AND t.superficie = 'HARD' THEN t.won ELSE 0 END)
      / NULLIF(SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) AND t.superficie = 'HARD' THEN 1 ELSE 0 END), 0), 2), 0) AS pct_dura_26m,

    SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) THEN 1 ELSE 0 END),
    SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 4 MONTH) THEN 1 ELSE 0 END),
    SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) AND t.superficie = 'CLAY' THEN 1 ELSE 0 END),
    SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) AND t.superficie = 'GRASS' THEN 1 ELSE 0 END),
    SUM(CASE WHEN t.fecha >= DATE_SUB(CURDATE(), INTERVAL 26 MONTH) AND t.superficie = 'HARD' THEN 1 ELSE 0 END)

FROM (
    -- ganadora
    SELECT p.pareja_ganadora_id AS pareja_id,
			tt.categoria,
           CASE WHEN UPPER(tt.superficie) LIKE '%CLAY%' THEN 'CLAY'
                WHEN UPPER(tt.superficie) LIKE '%GRASS%' THEN 'GRASS'
                ELSE 'HARD' END AS superficie,
           p.fecha, 1 AS won
    FROM partido p
    JOIN torneo tt ON p.torneo_id = tt.id
	WHERE p.pareja_ganadora_id IS NOT NULL
    UNION ALL
    -- perdedora
    SELECT p.pareja_perdedora_id,
			tt.categoria,
           CASE WHEN UPPER(tt.superficie) LIKE '%CLAY%' THEN 'CLAY'
                WHEN UPPER(tt.superficie) LIKE '%GRASS%' THEN 'GRASS'
                ELSE 'HARD' END AS superficie,
           p.fecha, 0 AS won
    FROM partido p
    JOIN torneo tt ON p.torneo_id = tt.id
    WHERE p.pareja_perdedora_id IS NOT NULL
) AS t

GROUP BY t.pareja_id, t.categoria
        """,
        nativeQuery = true
    )
    void insertEstadisticasParejas();
	
	@Modifying
    @Transactional
    @Query("DELETE FROM EstadisticasPareja")
    void borrarEstadisticasParejas();
}