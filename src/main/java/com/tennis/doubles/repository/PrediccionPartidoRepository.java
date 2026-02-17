package com.tennis.doubles.repository;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tennis.doubles.model.PrediccionPartido;

public interface PrediccionPartidoRepository extends JpaRepository<PrediccionPartido, Long> {
	
	@Query("""
        SELECT p FROM PrediccionPartido p
        JOIN Partido pa ON p.id = pa.id
        WHERE pa.fecha BETWEEN :desde AND :hasta
        order by pa.fecha asc
    """)
    List<PrediccionPartido> findPrediccionesEntreFechas(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
	
	@Query(value = """
        SELECT DISTINCT DATE_FORMAT(p.fecha, '%Y-%m') AS mes
        FROM prediccion_partido pr
        JOIN partido p ON pr.id = p.id
        ORDER BY mes DESC
        """, nativeQuery = true)
    List<String> findMesesConPronosticos();

    // 🔹 Total de pronósticos del mes
    @Query(value = """
        SELECT COUNT(*)
        FROM prediccion_partido pr
        JOIN partido p ON pr.id = p.id
        WHERE p.fecha BETWEEN :inicio AND :fin
        """, nativeQuery = true)
    int countPronosticos(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    // 🔹 Aciertos: cuando el pronóstico coincide con el ganador real
    @Query(value = """
        SELECT COUNT(*)
        FROM prediccion_partido pr
        JOIN partido p ON pr.id = p.id
        WHERE p.fecha BETWEEN :inicio AND :fin
          AND pr.pareja_ganadora_id = p.pareja_ganadora_id
        """, nativeQuery = true)
    int countAciertos(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    // 🔹 Fallos: cuando el pronóstico no coincide
    @Query(value = """
        SELECT COUNT(*)
        FROM prediccion_partido pr
        JOIN partido p ON pr.id = p.id
        WHERE p.fecha BETWEEN :inicio AND :fin
          AND pr.pareja_ganadora_id <> p.pareja_ganadora_id
        """, nativeQuery = true)
    int countFallos(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
    
 // Contar aciertos/fallos por rango de cuota
    @Query(value = """
        SELECT COUNT(*)
        FROM prediccion_partido pp
        JOIN partido p ON pp.id = p.id
        WHERE (:acertada IS NULL OR 
               ((:acertada = TRUE AND (p.pareja_ganadora_id = pp.pareja_ganadora_id)) OR
                (:acertada = FALSE AND (p.pareja_ganadora_id <> pp.pareja_ganadora_id))))
          AND pp.cuota > :minCuota
          AND pp.cuota <= :maxCuota
          AND YEAR(p.fecha) = :anio
          AND MONTH(p.fecha) = :mes
    """, nativeQuery = true)
    long countByCuotaBetweenAndFecha(@Param("anio") int anio,
                                     @Param("mes") int mes,
                                     @Param("minCuota") double minCuota,
                                     @Param("maxCuota") double maxCuota,
                                     @Param("acertada") Boolean acertada);

    // Contar aciertos/fallos cuando la cuota es NULL
    @Query(value = """
        SELECT COUNT(*)
        FROM prediccion_partido pp
        JOIN partido p ON pp.id = p.id
        WHERE (:acertada IS NULL OR 
               ((:acertada = TRUE AND (p.pareja_ganadora_id = pp.pareja_ganadora_id)) OR
                (:acertada = FALSE AND (p.pareja_ganadora_id <> pp.pareja_ganadora_id))))
          AND pp.cuota IS NULL
          AND YEAR(p.fecha) = :anio
          AND MONTH(p.fecha) = :mes
    """, nativeQuery = true)
    long countByCuotaIsNullAndFecha(@Param("anio") int anio,
                                    @Param("mes") int mes,
                                    @Param("acertada") Boolean acertada);
    
    @Query(value = """
        SELECT DISTINCT p.ronda
        FROM prediccion_partido pp
        JOIN partido p ON pp.id = p.id
        WHERE YEAR(p.fecha) = :anio
          AND MONTH(p.fecha) = :mes
        ORDER BY p.ronda
    """, nativeQuery = true)
    List<String> findRondasDisponibles(@Param("anio") int anio,
                                       @Param("mes") int mes);

    // Contar aciertos/fallos por ronda
    @Query(value = """
        SELECT COUNT(*)
        FROM prediccion_partido pp
        JOIN partido p ON pp.id = p.id
        WHERE (:acertada IS NULL OR
               ((:acertada = TRUE AND p.pareja_ganadora_id = pp.pareja_ganadora_id) OR
                (:acertada = FALSE AND p.pareja_ganadora_id <> pp.pareja_ganadora_id)))
          AND p.ronda = :ronda
          AND YEAR(p.fecha) = :anio
          AND MONTH(p.fecha) = :mes
    """, nativeQuery = true)
    long countByRondaAndFecha(@Param("anio") int anio,
                              @Param("mes") int mes,
                              @Param("ronda") String ronda,
                              @Param("acertada") Boolean acertada);
    
    @Query(value = """
            SELECT DISTINCT p.ronda
            FROM partido p
            JOIN prediccion_partido pp ON pp.id = p.id
            ORDER BY p.ronda
            """, nativeQuery = true)
    List<String> obtenerRondasDistintas();
    
    @Query(value = """
            SELECT DATE_FORMAT(p.fecha, '%m/%Y') AS mes,
                   SUM(CASE WHEN pp.pareja_ganadora_id = p.pareja_ganadora_id THEN 1 ELSE 0 END) AS aciertos,
                   COUNT(*) AS total
            FROM prediccion_partido pp
            JOIN partido p ON pp.id = p.id
            WHERE p.fecha >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
            GROUP BY DATE_FORMAT(p.fecha, '%m/%Y')
            ORDER BY p.fecha
            """, nativeQuery = true)
    List<Object[]> obtenerAciertosTotalesPorMes();

    @Query(value = """
            SELECT DATE_FORMAT(p.fecha, '%m/%Y') AS mes,
                   SUM(CASE WHEN pp.pareja_ganadora_id = p.pareja_ganadora_id THEN 1 ELSE 0 END) AS aciertos,
                   COUNT(*) AS total
            FROM prediccion_partido pp
            JOIN partido p ON pp.id = p.id
            WHERE ((:rango = '1-1.10' AND pp.cuota > 1 AND pp.cuota <= 1.10)
               OR (:rango = '1.10-1.20' AND pp.cuota > 1.10 AND pp.cuota <= 1.20)
               OR (:rango = '1.20-1.30' AND pp.cuota > 1.20 AND pp.cuota <= 1.30)
               OR (:rango = '1.30-1.40' AND pp.cuota > 1.30 AND pp.cuota <= 1.40)
               OR (:rango = '1.40-1.50' AND pp.cuota > 1.40 AND pp.cuota <= 1.50)
               OR (:rango = '>1.50' AND pp.cuota > 1.50)
               OR (:rango = 'sin cuota' AND pp.cuota IS NULL))
    		AND p.fecha >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
            GROUP BY DATE_FORMAT(p.fecha, '%m/%Y')
            ORDER BY p.fecha
            """, nativeQuery = true)
    List<Object[]> obtenerAciertosPorCuotaRango(@Param("rango") String rango);

    @Query(value = """
            SELECT DATE_FORMAT(p.fecha, '%m/%Y') AS mes,
                   SUM(CASE WHEN pp.pareja_ganadora_id = p.pareja_ganadora_id THEN 1 ELSE 0 END) AS aciertos,
                   COUNT(*) AS total
            FROM prediccion_partido pp
            JOIN partido p ON pp.id = p.id
            WHERE p.ronda = :ronda
            AND p.fecha >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
            GROUP BY DATE_FORMAT(p.fecha, '%m/%Y')
            ORDER BY p.fecha
            """, nativeQuery = true)
    List<Object[]> obtenerAciertosPorRonda(@Param("ronda") String ronda);
    
    @Query(value = """
            SELECT DATE_FORMAT(p.fecha, '%m/%Y') AS mes,
                   SUM(CASE WHEN pp.pareja_ganadora_id = p.pareja_ganadora_id THEN 1 ELSE 0 END) AS aciertos,
                   COUNT(*) AS total
            FROM prediccion_partido pp
            JOIN partido p ON pp.id = p.id
            JOIN pareja pw ON p.pareja_ganadora_id = pw.id
            JOIN jugador jw1 ON pw.jugador1_id = jw1.id
            WHERE p.fecha >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
              AND 'ATP' = jw1.categoria
            GROUP BY YEAR(p.fecha), MONTH(p.fecha)
            ORDER BY YEAR(p.fecha), MONTH(p.fecha)
            """, nativeQuery = true)
    List<Object[]> obtenerAciertosMasculinoPorMes();

    @Query(value = """
            SELECT DATE_FORMAT(p.fecha, '%m/%Y') AS mes,
                   SUM(CASE WHEN pp.pareja_ganadora_id = p.pareja_ganadora_id THEN 1 ELSE 0 END) AS aciertos,
                   COUNT(*) AS total
            FROM prediccion_partido pp
            JOIN partido p ON pp.id = p.id
            JOIN pareja pw ON p.pareja_ganadora_id = pw.id
            JOIN jugador jw1 ON pw.jugador1_id = jw1.id
            WHERE p.fecha >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
              AND 'WTA' = jw1.categoria
            GROUP BY YEAR(p.fecha), MONTH(p.fecha)
            ORDER BY YEAR(p.fecha), MONTH(p.fecha)
            """, nativeQuery = true)
    List<Object[]> obtenerAciertosFemeninoPorMes();
    
    @Query(value = """
            SELECT DATE_FORMAT(p.fecha, '%m/%Y') AS mes,
                   SUM(CASE WHEN pp.pareja_ganadora_id = p.pareja_ganadora_id THEN 1 ELSE 0 END) AS aciertos,
                   COUNT(*) AS total
            FROM prediccion_partido pp
            JOIN partido p ON pp.id = p.id
            JOIN torneo t ON p.torneo_id = t.id
            WHERE p.fecha >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
              AND t.categoria LIKE '%ITF%'
            GROUP BY YEAR(p.fecha), MONTH(p.fecha)
            ORDER BY YEAR(p.fecha), MONTH(p.fecha)
            """, nativeQuery = true)
    List<Object[]> obtenerAciertosITFPorMes();

    @Query(value = """
            SELECT DATE_FORMAT(p.fecha, '%m/%Y') AS mes,
                   SUM(CASE WHEN pp.pareja_ganadora_id = p.pareja_ganadora_id THEN 1 ELSE 0 END) AS aciertos,
                   COUNT(*) AS total
            FROM prediccion_partido pp
            JOIN partido p ON pp.id = p.id
            JOIN torneo t ON p.torneo_id = t.id
            WHERE p.fecha >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
              AND (t.categoria LIKE 'ATP' OR t.categoria LIKE 'WTA' OR t.categoria IS NULL)
            GROUP BY YEAR(p.fecha), MONTH(p.fecha)
            ORDER BY YEAR(p.fecha), MONTH(p.fecha)
            """, nativeQuery = true)
    List<Object[]> obtenerAciertosATPWTAPorMes();
    
    @Query(value = """
            SELECT DATE_FORMAT(p.fecha, '%m/%Y') AS mes,
                   SUM(CASE WHEN pp.pareja_ganadora_id = p.pareja_ganadora_id THEN 1 ELSE 0 END) AS aciertos,
                   COUNT(*) AS total
            FROM prediccion_partido pp
            JOIN partido p ON pp.id = p.id
            JOIN torneo t ON p.torneo_id = t.id
            WHERE p.fecha >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
              AND (t.categoria LIKE 'Challenger' OR t.categoria LIKE 'WTA 125')
            GROUP BY YEAR(p.fecha), MONTH(p.fecha)
            ORDER BY YEAR(p.fecha), MONTH(p.fecha)
            """, nativeQuery = true)
    List<Object[]> obtenerAciertosChallengerPorMes();


}
