package com.tennis.doubles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ConfiguracionCarga.class, PesosComparativaConfig.class})
public class DoublesTennisAnalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DoublesTennisAnalyzerApplication.class, args);
	}

}
