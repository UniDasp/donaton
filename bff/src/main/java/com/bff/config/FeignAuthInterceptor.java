package com.bff.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(FeignAuthInterceptor.class);

    private final JwtDecoder jwtDecoder;

    public FeignAuthInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public void apply(RequestTemplate template) {
        String path = template.url();
        if (path != null && (path.startsWith("/auth/login")
                || path.startsWith("/auth/register")
                || path.startsWith("/auth/refresh"))) {
            return;
        }

        Jwt jwt = resolveJwt();
        if (jwt == null) {
            log.warn("Feign call without JWT context: {} {}", template.method(), template.url());
            return;
        }

        template.header("Authorization", "Bearer " + jwt.getTokenValue());

        String email = jwt.getSubject();
        if (email != null && !email.isBlank()) {
            template.header("X-User-Email", email);
        }

        Object role = jwt.getClaim("role");
        if (role != null && !role.toString().isBlank()) {
            template.header("X-User-Role", role.toString());
        }
    }

    private Jwt resolveJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }

        String tokenValue = extractBearerFromCurrentRequest();
        if (tokenValue == null) {
            return null;
        }

        try {
            return jwtDecoder.decode(tokenValue);
        } catch (Exception ex) {
            log.warn("No se pudo decodificar JWT para Feign: {}", ex.getMessage());
            return null;
        }
    }

    private String extractBearerFromCurrentRequest() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }

        String token = authorization.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
