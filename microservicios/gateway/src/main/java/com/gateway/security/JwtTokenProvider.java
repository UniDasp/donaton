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
            log.debug("Extrayendo email del token JWT");
            String email = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            log.debug("Email extraído: {}", email);
            return email;
        } catch (ExpiredJwtException e) {
            log.warn(" Token expirado al extraer email: {}", e.getMessage());
            throw new JwtException("Token expirado");
        } catch (JwtException e) {
            log.warn(" Token inválido al extraer email: {}", e.getMessage());
            throw new JwtException("Token inválido");
        }
    }

    
    public String extractRole(String token) {
        try {
            log.debug("Extrayendo rol del token JWT");
            String role = (String) Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("role");
            log.debug("Rol extraído: {}", role);
            return role;
        } catch (JwtException e) {
            log.warn(" Error extrayendo rol del token: {}", e.getMessage());
            throw new JwtException("No se pudo extraer el rol del token");
        }
    }

   
    public boolean validateToken(String token) {
        try {
            log.debug("Validando token JWT");
            
            var claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            boolean isAccessToken = TOKEN_TYPE_ACCESS.equals(
                    claims.get("token_type", String.class)
            );
            
            if (isAccessToken) {
                log.debug(" Token JWT válido");
                return true;
            } else {
                log.warn(" Token no es de tipo ACCESS");
                return false;
            }
            
        } catch (ExpiredJwtException e) {
            log.warn(" Token expirado: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn(" Token inválido: {}", e.getMessage());
            return false;
        }
    }

    
    public long getExpirationTime() {
        return jwtExpirationMs;
    }
}
