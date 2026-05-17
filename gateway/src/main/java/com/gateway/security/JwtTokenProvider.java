package com.gateway.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:mi_clave_secreta_muy_larga_para_hs256_segura_12345}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    private static final String TOKEN_TYPE_ACCESS = "ACCESS";

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractEmail(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            log.warn("Token expirado: {}", e.getMessage());
            throw new JwtException("Token expirado");
        } catch (JwtException e) {
            log.warn("Token inválido: {}", e.getMessage());
            throw new JwtException("Token inválido");
        }
    }

    public String extractRole(String token) {
        try {
            return (String) Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("role");
        } catch (JwtException e) {
            log.warn("Error extrayendo rol del token: {}", e.getMessage());
            throw new JwtException("No se pudo extraer el rol del token");
        }
    }

    public boolean validateToken(String token) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return TOKEN_TYPE_ACCESS.equals(claims.get("token_type", String.class));
        } catch (ExpiredJwtException e) {
            log.warn("Token expirado");
            return false;
        } catch (JwtException e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    public long getExpirationTime() {
        return jwtExpirationMs;
    }
}
