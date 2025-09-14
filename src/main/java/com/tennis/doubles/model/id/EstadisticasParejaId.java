package com.tennis.doubles.model.id;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class EstadisticasParejaId implements Serializable {
    private Long parejaId;
    private String categoria;
}
