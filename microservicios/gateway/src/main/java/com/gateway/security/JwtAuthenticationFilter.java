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
        String method = exchange.getRequest().getMethod() == null
                ? ""
                : exchange.getRequest().getMethod().name();

        log.debug("[JWT-FILTER] Validando token - {} {}", method, path);

        try {
            String token = extractTokenFromRequest(exchange);

            if (token == null || token.isEmpty()) {
                log.warn("[JWT-FILTER]  Token no proporcionado - {}", path);
                return sendErrorResponse(exchange, HttpStatus.UNAUTHORIZED, 
                        "Token no proporcionado");
            }

            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("[JWT-FILTER]  Token inválido o expirado - {}", path);
                return sendErrorResponse(exchange, HttpStatus.UNAUTHORIZED, 
                        "Token inválido o expirado");
            }

            String email = jwtTokenProvider.extractEmail(token);
            String role = jwtTokenProvider.extractRole(token);

            log.info("[JWT-FILTER] Autenticación exitosa - Usuario: {} | Rol: {} | Ruta: {}", 
                    email, role, path);

          ServerWebExchange enrichedExchange = exchange.mutate()
                    .request(r -> r
                            .header("Authorization", "Bearer " + token)      
                            .header("X-User-Email", email)                   
                            .header("X-User-Role", role))                    
                    .build();

            log.debug("[JWT-FILTER] ✓ Petición autorizada - delegando a siguiente etapa");
            return chain.filter(enrichedExchange);

        } catch (JwtException e) {
            log.error("[JWT-FILTER]  Error JWT: {}", e.getMessage());
            return sendErrorResponse(exchange, HttpStatus.UNAUTHORIZED, 
                    "Token inválido: " + e.getMessage());
        } catch (Exception e) {
            log.error("[JWT-FILTER]  Error inesperado: {}", e.getMessage(), e);
            return sendErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error interno del servidor");
        }
    }

    
    private Mono<Void> sendErrorResponse(ServerWebExchange exchange, HttpStatus status, 
                                        String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().toString())
                .build();

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            return exchange.getResponse().writeWith(
                    Mono.fromCallable(() -> exchange.getResponse().bufferFactory().wrap(bytes))
            );
        } catch (Exception e) {
            log.error("[JWT-FILTER] Error serializando respuesta de error", e);
            return exchange.getResponse().setComplete();
        }
    }

   
    private String extractTokenFromRequest(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); 
            log.debug("[JWT-FILTER] Token extraído del header Authorization");
            return token;
        }
        
        log.debug("[JWT-FILTER] Header Authorization no encontrado o formato incorrecto");
        return null;
    }
}
