package com.gateway.security;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class JwtAuthenticationFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        return exchange.getPrincipal()
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .map(authentication -> {
                    String email = authentication.getToken().getClaimAsString("preferred_username");
                    var roles = authentication.getToken().getClaimAsStringList("roles");
                    
                    return exchange.mutate().request(request -> request
                            .header("X-User-Email", email == null ? "" : email)
                            .header("X-User-Role", roles == null ? "" : String.join(",", roles)))
                            .build();
                })
                .flatMap(mutatedExchange -> chain.filter(mutatedExchange))
                // SI NO HAY TOKEN: Continúa la cadena normal para que Spring Security lance el 401
                .switchIfEmpty(chain.filter(exchange)); 
    }

    private boolean isPublicPath(String path) {
        if (path == null) {
            return false;
        }
        return path.contains("/auth/login")
                || path.contains("/auth/register")
                || path.contains("/auth/refresh")
                || path.contains("/login")     
                || path.contains("/register")
                || path.contains("/health")
                || path.contains("/actuator/health");
    }
}