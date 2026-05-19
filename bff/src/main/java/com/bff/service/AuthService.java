package com.bff.service;

import com.bff.client.AuthClient;
import com.bff.dto.request.AuthRequest;
import com.bff.dto.request.RefreshRequest;
import com.bff.dto.request.RegisterRequest;
import com.bff.dto.response.AuthResponse;
import com.bff.dto.response.UserSummaryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final AuthClient authClient;

    public AuthService(AuthClient authClient) {
        this.authClient = authClient;
    }

    public AuthResponse login(AuthRequest request) {
        return authClient.login(request);
    }

    public UserSummaryResponse register(RegisterRequest request) {
        return authClient.register(request);
    }

    public AuthResponse refresh(RefreshRequest request) {
        return authClient.refresh(request);
    }

    public List<UserSummaryResponse> listUsers() {
        return authClient.listUsers();
    }
}
