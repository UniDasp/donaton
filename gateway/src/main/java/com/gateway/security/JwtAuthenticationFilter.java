package com.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.dto.ErrorResponse;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        try {
            String token = getTokenFromRequest(exchange);

            if (token == null || token.isEmpty()) {
                log.warn("Token no proporcionado para ruta: {}", path);
                return sendErrorResponse(exchange, HttpStatus.UNAUTHORIZED, 
                        "Token no proporcionado", path);
            }

            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("Token inválido para ruta: {}", path);
                return sendErrorResponse(exchange, HttpStatus.UNAUTHORIZED, 
                        "Token inválido o expirado", path);
            }

            String email = jwtTokenProvider.extractEmail(token);
            String role = jwtTokenProvider.extractRole(token);

            log.info("Usuario autenticado: {} con rol: {}", email, role);

            ServerWebExchange newExchange = exchange.mutate()
                    .request(r -> r.header("X-User-Email", email)
                            .header("X-User-Role", role))
                    .build();

            return chain.filter(newExchange);

        } catch (JwtException e) {
            log.error("Error de JWT: {}", e.getMessage());
            return sendErrorResponse(exchange, HttpStatus.UNAUTHORIZED, 
                    e.getMessage(), path);
        } catch (Exception e) {
            log.error("Error inesperado en autenticación: {}", e.getMessage());
            return sendErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error interno del servidor", path);
        }
    }

    private Mono<Void> sendErrorResponse(ServerWebExchange exchange, HttpStatus status, 
                                        String message, String path) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            return exchange.getResponse().writeWith(
                    Mono.fromCallable(() -> exchange.getResponse().bufferFactory().wrap(bytes))
            );
        } catch (Exception e) {
            log.error("Error serializando respuesta de error", e);
            return exchange.getResponse().setComplete();
        }
    }

    private String getTokenFromRequest(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean isPublicPath(String path) {
        return path.equals("/auth/login") || 
               path.equals("/auth/register") ||
               path.equals("/auth/refresh");
    }
}
