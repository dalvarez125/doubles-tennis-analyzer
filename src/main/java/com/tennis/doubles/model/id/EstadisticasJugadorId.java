package com.tennis.doubles.model.id;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class EstadisticasJugadorId implements Serializable {

    @Column(name = "jugador_id")
    private Long jugadorId;

    @Column(name = "categoria")
    private String categoria;
}
