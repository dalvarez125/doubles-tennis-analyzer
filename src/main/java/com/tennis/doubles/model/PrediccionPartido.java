package com.tennis.doubles.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "prediccion_partido")
@Getter
@Setter
public class PrediccionPartido {

    @Id
    private Long id; // mismo id que el partido, no autoincremental

    private Long parejaGanadoraId;

    private int puntosLocal;

    private int puntosVisitante;
    
    private Double cuota;
}
