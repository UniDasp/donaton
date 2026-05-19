package com.bff.client;

import com.bff.config.FeignClientConfig;
import com.bff.dto.request.AuthRequest;
import com.bff.dto.request.RefreshRequest;
import com.bff.dto.request.RegisterRequest;
import com.bff.dto.response.AuthResponse;
import com.bff.dto.response.UserSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "auth-service", configuration = FeignClientConfig.class)
public interface AuthClient {

    @PostMapping("/auth/login")
    AuthResponse login(@RequestBody AuthRequest request);

    @PostMapping("/auth/register")
    UserSummaryResponse register(@RequestBody RegisterRequest request);

    @PostMapping("/auth/refresh")
    AuthResponse refresh(@RequestBody RefreshRequest request);

    @GetMapping("/auth/users")
    List<UserSummaryResponse> listUsers();
}
