package com.donaton.logistics.client;

import com.donaton.logistics.dto.DonationDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DonationClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public DonationClient(@Value("${donation.base-url:http://donation:8080}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
    }

    public DonationDTO getDonationById(Long donationId, String email, String role) {
        String url = baseUrl + "/donations/" + donationId;
        ResponseEntity<DonationDTO> res = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(buildHeaders(email, role)),
                DonationDTO.class
        );
        return res.getBody();
    }

    private HttpHeaders buildHeaders(String email, String role) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Email", email);
        headers.add("X-User-Role", role);
        return headers;
    }
}
