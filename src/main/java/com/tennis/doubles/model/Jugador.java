package com.tennis.doubles.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Jugador {

    @Id
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "ranking_dobles")
    private Integer rankingDobles;

    @Column(nullable = false)
    private String categoria;
}
