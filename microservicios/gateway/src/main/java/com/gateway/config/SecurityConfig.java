package com.gateway.config;

import com.gateway.security.JwtAuthenticationFilter;
import com.gateway.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.WebFilter;

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
        return http
                .csrf().disable()
                .authorizeExchange(authz -> authz
                    .pathMatchers(
                            "/auth/login",
                            "/auth/register",
                            "/auth/refresh",
                            "/api/auth/login",
                            "/api/auth/register",
                            "/api/auth/refresh",
                            "/health",
                            "/actuator/health"
                    ).permitAll()
                    .anyExchange().permitAll())
                .httpBasic().disable()
                .formLogin().disable()
                .logout().disable()
                .build();
    }

    @Bean
    public WebFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }
}


