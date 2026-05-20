package com.bff.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class BearerJwtAuthFilter extends OncePerRequestFilter {

    private static final String TOKEN_TYPE_ACCESS = "ACCESS";

    private final SecretKey signingKey;

    public BearerJwtAuthFilter(@Value("${JWT_SECRET:${jwt.secret}}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return isPublicPath(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing bearer token");
            return;
        }

        String rawToken = authorization.substring(7).trim();
        if (rawToken.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing bearer token");
            return;
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(rawToken)
                    .getPayload();

            if (!TOKEN_TYPE_ACCESS.equals(claims.get("token_type", String.class))) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token type");
                return;
            }

            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            Jwt jwt = buildSpringJwt(rawToken, claims, email, role);
            List<SimpleGrantedAuthority> authorities = role != null && !role.isBlank()
                    ? List.of(new SimpleGrantedAuthority("ROLE_" + role.trim()))
                    : List.of();
            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, authorities));
            filterChain.doFilter(request, response);
        } catch (JwtException ex) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        }
    }

    private Jwt buildSpringJwt(String rawToken, Claims claims, String email, String role) {
        Instant issuedAt = toInstant(claims.getIssuedAt());
        Instant expiresAt = toInstant(claims.getExpiration());

        Jwt.Builder builder = Jwt.withTokenValue(rawToken)
                .header("alg", "HS256")
                .subject(email)
                .claim("role", role)
                .claim("token_type", TOKEN_TYPE_ACCESS);

        if (issuedAt != null) {
            builder.issuedAt(issuedAt);
        }
        if (expiresAt != null) {
            builder.expiresAt(expiresAt);
        }

        return builder.build();
    }

    private Instant toInstant(Date date) {
        return date == null ? null : date.toInstant();
    }

    private boolean isPublicPath(String path) {
        if (path == null) {
            return false;
        }
        return path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/auth/register")
                || path.equals("/api/v1/auth/refresh")
                || path.equals("/actuator/health");
    }
}
