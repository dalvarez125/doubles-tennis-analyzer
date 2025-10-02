package com.tennis.doubles.dto.cuotas;
import lombok.Data;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CuotasDTO {
    private int structureType;
    private int marketId;
    private String marketName;
    private boolean isLive;
    private long fid;
    private boolean suspended;
    private long id;
    private String marketGroup;
    private String marketPeriod;
    private List<OpcionCuotaDTO> choices;
}
