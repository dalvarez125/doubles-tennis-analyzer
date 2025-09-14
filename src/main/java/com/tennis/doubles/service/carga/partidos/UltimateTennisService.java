package com.tennis.doubles.service.carga.partidos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennis.doubles.dto.carga.ranking.RankingDTO;
import com.tennis.doubles.dto.carga.ranking.RankingResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
public class UltimateTennisService {

    @Value("${allsportsapi.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String API_HOST = "ultimate-tennis1.p.rapidapi.com";

    public List<RankingDTO> obtenerRanking(String categoria) throws IOException, InterruptedException {
        String url = construirUrl(categoria);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-rapidapi-key", apiKey)
                .header("x-rapidapi-host", API_HOST)
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        String json = response.body();

        try {
            RankingResponse rankingResponse = objectMapper.readValue(json, RankingResponse.class);
            return rankingResponse.getData();
        } catch (JsonProcessingException e) {
            // O guardar el JSON en un archivo para analizarlo luego
            Files.writeString(Path.of("respuesta_error.json"), json);
            return Collections.emptyList(); // o lanzar una excepción controlada
        }
    }

    private String construirUrl(String categoria) {
    	LocalDate hoy = LocalDate.now();
        LocalDate ultimoLunes = hoy.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        String fechaUltimoLunes = ultimoLunes.format(DateTimeFormatter.ISO_LOCAL_DATE);
        return "https://" + API_HOST + "/rankings/" + categoria + "/doubles/30/" + fechaUltimoLunes;
    }
}
