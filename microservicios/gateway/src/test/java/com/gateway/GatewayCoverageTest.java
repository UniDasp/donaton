package com.gateway;

import com.gateway.exception.JwtAuthenticationException;
import com.gateway.security.JwtAuthenticationFilter;
import com.gateway.security.JwtTokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Mono.empty;
//hola
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class GatewayCoverageTest {

    private static final String SECRET = "mi_clave_secreta_muy_larga_para_hs256_segura_12345";

    @Autowired
    private SecurityWebFilterChain securityWebFilterChain;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void contextLoadsAndRegistersSecurityBeans() {
        assertNotNull(securityWebFilterChain);
        assertNotNull(jwtTokenProvider);
        assertDoesNotThrow(() -> new Application());
    }

    @Test
    void providerValidatesAndExtractsClaimsAndErrors() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(provider, "jwtExpirationMs", 86_400_000L);

        SecretKey signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        String accessToken = Jwts.builder()
                .subject("user@example.com")
                .claim("role", "ADMIN")
                .claim("token_type", "ACCESS")
                .signWith(signingKey)
                .compact();

        String refreshToken = Jwts.builder()
                .subject("user@example.com")
                .claim("role", "USER")
                .claim("token_type", "REFRESH")
                .signWith(signingKey)
                .compact();

        String expiredToken = Jwts.builder()
                .subject("expired@example.com")
                .claim("role", "USER")
                .claim("token_type", "ACCESS")
                .expiration(java.util.Date.from(Instant.now().minusSeconds(60)))
                .signWith(signingKey)
                .compact();

        String invalidRoleToken = Jwts.builder()
                .subject("user@example.com")
                .claim("role", "AUDITOR")
                .claim("token_type", "ACCESS")
                .signWith(signingKey)
                .compact() + "tampered";

        assertTrue(provider.validateToken(accessToken));
        assertEquals("user@example.com", provider.extractEmail(accessToken));
        assertEquals("ADMIN", provider.extractRole(accessToken));
        assertEquals(86_400_000L, provider.getExpirationTime());
        assertFalse(provider.validateToken(refreshToken));
        assertFalse(provider.validateToken(expiredToken));
        assertThrows(JwtException.class, () -> provider.extractEmail(expiredToken));
        assertThrows(JwtException.class, () -> provider.extractRole(invalidRoleToken));
        assertEquals("boom", new JwtAuthenticationException("boom").getMessage());
        assertEquals("cause", new JwtAuthenticationException("boom", new IllegalStateException("cause")).getCause().getMessage());
    }

    @Test
    void filterAllowsPublicPathAndEnrichesAuthorizedRequests() {
        JwtTokenProvider provider = mock(JwtTokenProvider.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(provider);

        WebFilterChain publicChain = mock(WebFilterChain.class);
        when(publicChain.filter(any())).thenReturn(empty());
        MockServerWebExchange publicExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/auth/login").build()
        );

        assertDoesNotThrow(() -> filter.filter(publicExchange, publicChain).block());
        verify(publicChain).filter(publicExchange);
        verifyNoInteractions(provider);

        String token = "valid-token";
        WebFilterChain protectedChain = mock(WebFilterChain.class);
        AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();

        when(provider.validateToken(token)).thenReturn(true);
        when(provider.extractEmail(token)).thenReturn("user@example.com");
        when(provider.extractRole(token)).thenReturn("ADMIN");
        when(protectedChain.filter(any())).thenAnswer(invocation -> {
            capturedExchange.set(invocation.getArgument(0));
            return empty();
        });

        MockServerWebExchange protectedExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/donations")
                        .header("Authorization", "Bearer " + token)
                        .build()
        );

        assertDoesNotThrow(() -> filter.filter(protectedExchange, protectedChain).block());
        verify(provider).validateToken(token);
        verify(provider).extractEmail(token);
        verify(provider).extractRole(token);
        assertNotNull(capturedExchange.get());
        assertEquals("Bearer " + token, capturedExchange.get().getRequest().getHeaders().getFirst("Authorization"));
        assertEquals("user@example.com", capturedExchange.get().getRequest().getHeaders().getFirst("X-User-Email"));
        assertEquals("ADMIN", capturedExchange.get().getRequest().getHeaders().getFirst("X-User-Role"));
    }

    @Test
    void filterReturnsErrorsForMissingInvalidAndUnexpectedFailures() {
        JwtTokenProvider provider = mock(JwtTokenProvider.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(provider);

        WebFilterChain chain = mock(WebFilterChain.class);

        MockServerWebExchange missingTokenExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/donations").build()
        );

        assertDoesNotThrow(() -> filter.filter(missingTokenExchange, chain).block());
        assertEquals(401, missingTokenExchange.getResponse().getStatusCode().value());
        verifyNoInteractions(provider);
        verify(chain, never()).filter(any());

        when(provider.validateToken("bad-token")).thenReturn(false);
        MockServerWebExchange invalidTokenExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/donations")
                        .header("Authorization", "Bearer bad-token")
                        .build()
        );

        assertDoesNotThrow(() -> filter.filter(invalidTokenExchange, chain).block());
        assertEquals(401, invalidTokenExchange.getResponse().getStatusCode().value());

        MockServerWebExchange errorExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/donations")
                        .build()
        );

        assertDoesNotThrow(() -> {
            Object result = ReflectionTestUtils.invokeMethod(
                    filter,
                    "sendErrorResponse",
                    errorExchange,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "boom"
            );
            assertNotNull(result);
            ((reactor.core.publisher.Mono<Void>) result).block();
        });
        assertEquals(500, errorExchange.getResponse().getStatusCode().value());
    }
}