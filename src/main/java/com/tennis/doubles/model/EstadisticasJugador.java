package com.tennis.doubles.model;
import com.tennis.doubles.model.id.EstadisticasJugadorId;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "estadisticas_jugador")
@Getter
@Setter
public class EstadisticasJugador {
	
	@EmbeddedId
    private EstadisticasJugadorId id;

    @Column(name = "pct_total_26m")
    private Double porcentajeTotal26m;

    @Column(name = "pct_total_4m")
    private Double porcentajeTotal4m;

    @Column(name = "pct_tierra_26m")
    private Double porcentajeTierra26m;

    @Column(name = "pct_hierba_26m")
    private Double porcentajeHierba26m;

    @Column(name = "pct_dura_26m")
    private Double porcentajeDura26m;
    
    @Column(name = "partidos_total_26m")
    private int partidosTotal26m;

    @Column(name = "partidos_total_4m")
    private int partidosTotal4m;

    @Column(name = "partidos_tierra_26m")
    private int partidosTierra26m;

    @Column(name = "partidos_hierba_26m")
    private int partidosHierba26m;

    @Column(name = "partidos_dura_26m")
    private int partidosDura26m;
}