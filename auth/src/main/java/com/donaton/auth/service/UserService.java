package com.donaton.auth.service;

import com.donaton.auth.dto.TokenResponseDTO;
import com.donaton.auth.model.User;
import com.donaton.auth.repository.UserRepository;
import com.donaton.auth.security.JwtService;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repository;
    private final JwtService jwtService;

    public UserService(UserRepository repository, JwtService jwtService) {
        this.repository = repository;
        this.jwtService = jwtService;
    }

    public User registrar(User user) {
        return repository.save(user);
    }

    public TokenResponseDTO login(String email, String password) {

        User user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getRole().name());

        return TokenResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessExpiresIn(900000L)
                .refreshExpiresIn(604800000L)
                .build();
    }

    public TokenResponseDTO refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("Refresh token no proporcionado");
        }

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Token de refresco inválido");
        }

        String email = jwtService.extractEmail(refreshToken);
        String role = jwtService.extractRole(refreshToken);

        String newAccessToken = jwtService.generateAccessToken(email, role);

        return TokenResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessExpiresIn(900000L)
                .refreshExpiresIn(604800000L)
                .build();
    }
}