package com.tennis.doubles.service.carga.partidos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennis.doubles.dto.carga.partidos.EventoDTO;
import com.tennis.doubles.dto.carga.partidos.RespuestaEventosDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class AllSportsApiService {

    @Value("${allsportsapi.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String API_HOST = "allsportsapi2.p.rapidapi.com";

    public List<EventoDTO> obtenerEventosPorFecha(LocalDate fecha) throws IOException, InterruptedException {
        String url = construirUrlParaFecha(fecha);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-rapidapi-key", apiKey)
                .header("x-rapidapi-host", API_HOST)
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        String json = response.body();

        try {
            RespuestaEventosDTO respuesta = objectMapper.readValue(json, new TypeReference<>() {});
            return respuesta.getEvents();
        } catch (JsonProcessingException e) {
            // O guardar el JSON en un archivo para analizarlo luego
            Files.writeString(Path.of("respuesta_error.json"), json);
            return Collections.emptyList(); // o lanzar una excepción controlada
        }
    }

    private String construirUrlParaFecha(LocalDate fecha) {
        int dia = fecha.getDayOfMonth();
        int mes = fecha.getMonthValue();
        int anio = fecha.getYear();
        return "https://" + API_HOST + "/api/tennis/events/" + dia + "/" + mes + "/" + anio;
    }
}
