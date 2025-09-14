package com.tennis.doubles.dto.carga.partidos;
import lombok.Data;

@Data
public class ResumenComparativaDTO {

    private int puntosLocal;
    private int puntosVisitante;

    private int puntosParejaHabitualLocal;
    private int puntosParejaHabitualVisitante;

    private int puntosFormaRecienteLocal;
    private int puntosFormaRecienteVisitante;
    
    private int puntosRankingLocal;
    private int puntosRankingVisitante;

    private int puntosVictoriasTotalesLocal;
    private int puntosVictoriasTotalesVisitante;

    private int puntosSuperficieLocal;
    private int puntosSuperficieVisitante;

    private int puntosHistorialVsRivalLocal;
    private int puntosHistorialVsRivalVisitante;

    private int puntosExperienciaIndividualLocal;
    private int puntosExperienciaIndividualVisitante;

    private boolean parejaLocalEsHabitual;
    private boolean parejaVisitanteEsHabitual;

    private double porcentajeFormaRecienteLocal;
    private double porcentajeFormaRecienteVisitante;

    private double porcentajeTotalLocal;
    private double porcentajeTotalVisitante;

    private double porcentajeSuperficieLocal;
    private double porcentajeSuperficieVisitante;

    private int partidosGanadosContraRivalLocal;
    private int partidosGanadosContraRivalVisitante;

    private int partidosJugadosJuntosLocal;
    private int partidosJugadosJuntosVisitante;

    private int partidosTotalesJugador1Local;
    private int partidosTotalesJugador2Local;
    private int partidosTotalesJugador1Visitante;
    private int partidosTotalesJugador2Visitante;
    
    private Integer rankingJugador1Local;
    private Integer rankingJugador2Local;
    private Integer rankingJugador1Visitante;
    private Integer rankingJugador2Visitante;
}
