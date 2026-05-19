package com.donaton.donation.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class LogisticsClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public LogisticsClient(@Value("${logistics.base-url:http://logistics:8080}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
    }

    public void crearEnvio(Long donacionId, String email, String role) {
        String url = baseUrl + "/envios";
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Email", email == null || email.isBlank() ? "donation@donaton.internal" : email);
        headers.add("X-User-Role", role == null || role.isBlank() ? "ADMIN" : role);
        headers.add("Content-Type", "application/json");

        Map<String, Object> request = Map.of("donacionId", donacionId);
        restTemplate.postForObject(url, new HttpEntity<>(request, headers), String.class);
    }
}
