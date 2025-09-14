package com.tennis.doubles.model;
import com.tennis.doubles.model.id.EstadisticasParejaId;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "estadisticas_pareja")
@Getter
@Setter
public class EstadisticasPareja {
	@EmbeddedId
    private EstadisticasParejaId id;

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
}