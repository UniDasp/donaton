package com.donaton.auth.controller;

import com.donaton.auth.dto.AuthDTO;
import com.donaton.auth.dto.RefreshTokenDTO;
import com.donaton.auth.dto.TokenResponseDTO;
import com.donaton.auth.dto.RoleUpdateRequestDTO;
import com.donaton.auth.dto.UserSummaryDTO;
import com.donaton.auth.model.User;
import com.donaton.auth.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService service;

    public AuthController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public UserSummaryDTO register(@RequestBody User user) {
        return service.registrarPublic(user);
    }

    @GetMapping("/users")
    public List<UserSummaryDTO> listarUsuarios(@RequestHeader("X-User-Role") String role) {
        return service.listarUsuarios(role);
    }

    @PostMapping("/users")
    public UserSummaryDTO crearUsuario(
            @RequestBody User user,
            @RequestHeader("X-User-Role") String role
    ) {
        return service.crearUsuario(role, user);
    }

    @PutMapping("/users/{id}/role")
    public UserSummaryDTO cambiarRol(
            @PathVariable Long id,
            @RequestBody RoleUpdateRequestDTO request,
            @RequestHeader("X-User-Role") String role
    ) {
        return service.cambiarRol(id, request, role);
    }

    @DeleteMapping("/users/{id}")
    public void eliminarUsuario(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role
    ) {
        service.eliminarUsuario(id, role);
    }

    @PostMapping("/login")
    public TokenResponseDTO login(@RequestBody AuthDTO dto) {
        return service.login(dto.getEmail(), dto.getPassword());
    }

    @PostMapping("/refresh")
    public TokenResponseDTO refresh(@RequestBody RefreshTokenDTO dto) {
        return service.refresh(dto.getRefreshToken());
    }
}