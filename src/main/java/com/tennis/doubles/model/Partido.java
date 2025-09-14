package com.tennis.doubles.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Partido {

    @Id
    private Long id;

    private LocalDate fecha;

    private String ronda;

    private String marcador;

    @ManyToOne
    private Pareja parejaGanadora;

    @ManyToOne
    private Pareja parejaPerdedora;

    @ManyToOne
    private Torneo torneo;
}
