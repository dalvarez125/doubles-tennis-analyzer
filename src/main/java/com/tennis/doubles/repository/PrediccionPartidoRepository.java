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
}
