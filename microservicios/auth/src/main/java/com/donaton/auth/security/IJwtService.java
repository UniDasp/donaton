package com.donaton.auth.security;

public interface IJwtService {
    String generateAccessToken(String email, String role);
    String generateRefreshToken(String email, String role);
    boolean isRefreshToken(String token);
    String extractEmail(String token);
    String extractRole(String token);
    String extractTokenType(String token);
}