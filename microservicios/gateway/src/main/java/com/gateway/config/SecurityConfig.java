package com.gateway.config;

import com.gateway.security.JwtAuthenticationFilter;
import com.gateway.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Slf4j
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    
    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        log.info("[GATEWAY] Inicializando cadena de filtros de seguridad perimetral");
        
        return http
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                
                .authorizeExchange(authz -> authz
                    
                    
                    .pathMatchers(
                            "/auth/login",
                            "/auth/register",
                            "/auth/refresh",
                            "/api/auth/login",
                            "/api/auth/register",
                            "/api/auth/refresh",
                            "/health",
                            "/actuator/health",
                            "/swagger-ui/**",
                            "/v3/api-docs/**"
                    ).permitAll()
                   .anyExchange().authenticated()
                )
                
                .addFilterAt(
                    new JwtAuthenticationFilter(jwtTokenProvider),
                    SecurityWebFiltersOrder.AUTHENTICATION
                )
                
                
                
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                
                .build();
    }
}



