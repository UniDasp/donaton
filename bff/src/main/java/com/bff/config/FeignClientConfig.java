package com.bff.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor feignAuthRequestInterceptor() {
        return template -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                String token = jwtAuth.getToken().getTokenValue();
                template.header("Authorization", "Bearer " + token);

                String email = jwtAuth.getToken().getSubject();
                if (email != null && !email.isBlank()) {
                    template.header("X-User-Email", email);
                }

                Object role = jwtAuth.getToken().getClaim("role");
                if (role != null) {
                    template.header("X-User-Role", role.toString());
                }
            }
        };
    }
}
