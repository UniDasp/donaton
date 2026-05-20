package com.bff.controller;

import com.bff.dto.request.AdminUserRequest;
import com.bff.dto.request.AuthRequest;
import com.bff.dto.request.RefreshRequest;
import com.bff.dto.request.RegisterRequest;
import com.bff.dto.request.RoleUpdateRequest;
import com.bff.dto.response.AuthResponse;
import com.bff.dto.response.UserSummaryResponse;
import com.bff.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserSummaryResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @GetMapping("/users")
    public List<UserSummaryResponse> listUsers() {
        return authService.listUsers();
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserSummaryResponse createUser(
            @Valid @RequestBody AdminUserRequest request
    ) {
        return authService.createUser(request);
    }

    @PutMapping("/users/{id}/role")
    public UserSummaryResponse updateRole(
            @PathVariable Long id,
            @RequestBody RoleUpdateRequest request
    ) {
        return authService.updateRole(id, request);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        authService.deleteUser(id);
    }
}