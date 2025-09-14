package com.tennis.doubles.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Torneo {

    @Id
    private String id;

    private String nombre;

    private String superficie;
    
    private String categoria;

    private int anio;
}
