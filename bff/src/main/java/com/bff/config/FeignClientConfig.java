package com.bff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FeignClientConfig {

    
    @Bean
    public RequestInterceptor feignAuthRequestInterceptor() {
        return template -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                String authHeader = request.getHeader("Authorization");
                String userEmail = request.getHeader("X-User-Email");
                String userRole = request.getHeader("X-User-Role");

                if (authHeader != null && !authHeader.isBlank()) {
                    template.header("Authorization", authHeader);
                    log.debug("[FEIGN-INTERCEPTOR] Propagando Authorization header");
                }
                if (userEmail != null && !userEmail.isBlank()) {
                    template.header("X-User-Email", userEmail);
                    log.debug("[FEIGN-INTERCEPTOR] Propagando X-User-Email: {}", userEmail);
                }
                if (userRole != null && !userRole.isBlank()) {
                    template.header("X-User-Role", userRole);
                    log.debug("[FEIGN-INTERCEPTOR] Propagando X-User-Role: {}", userRole);
                }
            } else {
                log.warn("[FEIGN-INTERCEPTOR] RequestAttributes no disponible - sin headers para propagar");
            }
        };
    }
}
