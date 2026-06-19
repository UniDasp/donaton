package com.bff.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(FeignAuthInterceptor.class);

    public FeignAuthInterceptor() {
    }

    @Override
    public void apply(RequestTemplate template) {
        String path = template.url();
        if (path != null && (path.startsWith("/auth/login")
                || path.startsWith("/auth/register")
                || path.startsWith("/auth/refresh"))) {
            log.debug("[FEIGN] Ruta pública - Sin headers de propagación: {}", path);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.debug("[FEIGN] Sin autenticación en SecurityContext");
            return;
        }

        if (authentication instanceof UsernamePasswordAuthenticationToken upAuth) {
            String email = (String) upAuth.getPrincipal();
            if (email != null && !email.isBlank()) {
                template.header("X-User-Email", email);
                log.debug("[FEIGN] Propagando X-User-Email: {} a {}", email, path);
            }

            String role = extractRoleFromAuthorities(upAuth);
            if (role != null && !role.isBlank()) {
                template.header("X-User-Role", role);
                log.debug("[FEIGN] Propagando X-User-Role: {} a {}", role, path);
            }
        }
    }

    private String extractRoleFromAuthorities(UsernamePasswordAuthenticationToken authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5))
                .findFirst()
                .orElse(null);
    }
}
