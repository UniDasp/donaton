package com.donaton.donation.client;

import com.donaton.donation.dto.NeedDTO;
import com.donaton.donation.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NeedsClient {

    private final RestTemplate restTemplate;

    private final String baseUrl;

    public NeedsClient(@Value("${needs.base-url:http://needs:8080}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
    }

    public NeedDTO getNeedById(String needId, String email, String role) {
        try {
            String url = baseUrl + "/needs/" + needId;
            ResponseEntity<NeedDTO> res = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(buildHeaders(email, role)),
                    NeedDTO.class
            );
            return res.getBody();
        } catch (Exception ex) {
            throw new BadRequestException("No se pudo validar la necesidad");
        }
    }

    public void rollbackReceive(String needId, Double amount) {
        try {
            String url = baseUrl + "/needs/" + needId + "/rollback?amount=" + amount;
            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    new HttpEntity<>(serviceHeaders()),
                    String.class
            );
        } catch (Exception ex) {
            throw new BadRequestException("No se pudo revertir la necesidad");
        }
    }

    public void receiveDonation(String needId, Double amount, String email, String role) {
        try {
            String url = baseUrl + "/needs/" + needId + "/receive?amount=" + amount;
            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    new HttpEntity<>(buildHeaders(email, role)),
                    String.class
            );
        } catch (Exception ex) {
            throw new BadRequestException("No se pudo actualizar la necesidad con la donación");
        }
    }

    private HttpHeaders buildHeaders(String email, String role) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Email", email);
        headers.add("X-User-Role", role);
        return headers;
    }

    private HttpHeaders serviceHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Email", "donation@donaton.internal");
        headers.add("X-User-Role", "ADMIN");
        return headers;
    }
}
