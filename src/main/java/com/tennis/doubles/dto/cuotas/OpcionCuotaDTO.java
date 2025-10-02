package com.tennis.doubles.dto.cuotas;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpcionCuotaDTO {
    private String initialFractionalValue;
    private String fractionalValue;
    private long sourceId;
    private String name;
    private boolean winning;
    private int change;
}
