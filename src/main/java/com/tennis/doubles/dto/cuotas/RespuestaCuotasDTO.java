package com.tennis.doubles.dto.cuotas;
import lombok.Data;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespuestaCuotasDTO {
    private Map<String, CuotasDTO> odds;
}
