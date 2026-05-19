package com.donaton.donation.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.PUT, "/donations/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/donations/**").hasRole("ADMIN")
                .anyRequest().authenticated()
                )
                .addFilterBefore(gatewayAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public OncePerRequestFilter gatewayAuthFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                String email = request.getHeader("X-User-Email");
                String role = request.getHeader("X-User-Role");

                if (email == null || email.isBlank()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
                    return;
                }

                var authorities = role == null || role.isBlank()
                        ? List.<SimpleGrantedAuthority>of()
                        : List.of(new SimpleGrantedAuthority("ROLE_" + role.trim()));

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(email, null, authorities)
                );

                filterChain.doFilter(request, response);
            }
        };
    }
}