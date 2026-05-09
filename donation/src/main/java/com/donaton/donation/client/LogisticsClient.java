package com.donaton.donation.client;

import com.donaton.donation.dto.LogisticsRequestDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class LogisticsClient {

    private final RestTemplate restTemplate;

    public LogisticsClient() {
        this.restTemplate = new RestTemplate();
    }

    public void crearEnvio(Long donacionId, String direccion) {

        String url = "http://localhost:8083/envios";

        Map<String, Object> request = Map.of(
                "donacionId", donacionId,
                "direccion", direccion
        );

        restTemplate.postForObject(url, request, String.class);
    }

    public void crearEnvio(LogisticsRequestDTO logisticsRequest) {
    }
}