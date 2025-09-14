package com.tennis.doubles.model;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "actualizacion")
@Data
public class Actualizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaUltimaActualizacion;

    @Column(length = 10)
    private String estado; // "OK", "KO", "PARCIAL"
}
