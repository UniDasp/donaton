package com.bff.client;

import com.bff.dto.request.AdminUserRequest;
import com.bff.dto.request.AuthRequest;
import com.bff.dto.request.RefreshRequest;
import com.bff.dto.request.RegisterRequest;
import com.bff.dto.request.RoleUpdateRequest;
import com.bff.dto.response.AuthResponse;
import com.bff.dto.response.UserSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @PostMapping("/auth/login")
    AuthResponse login(@RequestBody AuthRequest request);

    @PostMapping("/auth/register")
    UserSummaryResponse register(@RequestBody RegisterRequest request);

    @PostMapping("/auth/refresh")
    AuthResponse refresh(@RequestBody RefreshRequest request);

    @GetMapping("/auth/users")
    List<UserSummaryResponse> listUsers();

    @PostMapping("/auth/users")
    UserSummaryResponse createUser(@RequestBody AdminUserRequest request);

    @PutMapping("/auth/users/{id}/role")
    UserSummaryResponse updateRole(
            @PathVariable Long id,
            @RequestBody RoleUpdateRequest request
    );

    @DeleteMapping("/auth/users/{id}")
    void deleteUser(@PathVariable Long id);
}