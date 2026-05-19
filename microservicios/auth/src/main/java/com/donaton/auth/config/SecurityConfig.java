package com.donaton.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

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
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/login", "/register", "/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register", "/auth/refresh").permitAll()
                .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers("/auth/users/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(gatewayAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public OncePerRequestFilter gatewayAuthFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                String path = request.getRequestURI();
                String method = request.getMethod();

                if ("OPTIONS".equalsIgnoreCase(method) || isPublicAuthPath(path)) {
                    filterChain.doFilter(request, response);
                    return;
                }

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
                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(email, null, authorities)
                );

                filterChain.doFilter(request, response);
            }

            private boolean isPublicAuthPath(String path) {
                if (path == null || path.isBlank()) return false;
                String normalized = path.endsWith("/") && path.length() > 1
                        ? path.substring(0, path.length() - 1)
                        : path;

                return normalized.equals("/login")
                        || normalized.equals("/register")
                        || normalized.equals("/refresh")
                        || normalized.equals("/auth/login")
                        || normalized.equals("/auth/register")
                        || normalized.equals("/auth/refresh");
            }
        };
    }
}