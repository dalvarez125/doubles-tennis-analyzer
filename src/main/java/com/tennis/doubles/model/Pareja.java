package com.tennis.doubles.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Pareja {

    @Id
    private Long id;

    @ManyToOne
    private Jugador jugador1;

    @ManyToOne
    private Jugador jugador2;

    @Column(nullable = false)
    private boolean favorito = false;
}

