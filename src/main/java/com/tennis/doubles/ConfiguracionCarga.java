package com.tennis.doubles;

import java.time.LocalDate;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "tenis.carga")
@Getter
@Setter
public class ConfiguracionCarga {

    private LocalDate inicio;
    private LocalDate fin;
}