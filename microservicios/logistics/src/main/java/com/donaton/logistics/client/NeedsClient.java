package com.donaton.logistics.client;

import com.donaton.logistics.dto.NeedDTO;
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
        String url = baseUrl + "/needs/" + needId;
        ResponseEntity<NeedDTO> res = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(buildHeaders(email, role)),
                NeedDTO.class
        );
        return res.getBody();
    }

    public void rollbackReceive(String needId, Double amount) {
        String url = baseUrl + "/needs/" + needId + "/rollback?amount=" + amount;
        restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(serviceHeaders()),
                Void.class
        );
    }

    private HttpHeaders buildHeaders(String email, String role) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Email", email);
        headers.add("X-User-Role", role);
        return headers;
    }

    private HttpHeaders serviceHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Email", "logistics@donaton.internal");
        headers.add("X-User-Role", "ADMIN");
        return headers;
    }
}
