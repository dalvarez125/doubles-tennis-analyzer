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
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long parejaId;
    private String categoria;
}
