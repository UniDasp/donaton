package com.bff;

import com.bff.client.AuthClient;
import com.bff.client.DonationClient;
import com.bff.client.LogisticsClient;
import com.bff.client.NeedsClient;
import com.bff.config.FeignAuthInterceptor;
import com.bff.config.WebConfig;
import com.bff.dto.request.AdminUserRequest;
import com.bff.dto.request.AuthRequest;
import com.bff.dto.request.DonationRequest;
import com.bff.dto.request.LogisticsRequest;
import com.bff.dto.request.NeedsRequest;
import com.bff.dto.request.RefreshRequest;
import com.bff.dto.request.RegisterRequest;
import com.bff.dto.request.RoleUpdateRequest;
import com.bff.dto.response.AuthResponse;
import com.bff.dto.response.DonationResponse;
import com.bff.dto.response.LogisticsResponse;
import com.bff.dto.response.NeedsResponse;
import com.bff.dto.response.UserSummaryResponse;
import com.bff.exception.ApiError;
import com.bff.exception.BFFException;
import com.bff.exception.GlobalExceptionHandler;
import com.bff.security.GatewayAuthFilter;
import com.bff.service.AuthService;
import com.bff.service.DonationService;
import com.bff.service.LogisticsService;
import com.bff.service.NeedsService;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BffCoverageTest {

    private static final String USER_EMAIL = "user@example.com";
    private static final String USER_ROLE = "ADMIN";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private DonationService donationService;

    @MockBean
    private LogisticsService logisticsService;

    @MockBean
    private NeedsService needsService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authEndpointsAndValidationCoverControllerAndHandler() throws Exception {
        AuthResponse authResponse = authResponse("access-token", "refresh-token");
        UserSummaryResponse userResponse = userResponse(1L, "Ada", "ada@example.com", "555", "ADMIN");

        when(authService.login(any())).thenReturn(authResponse);
        when(authService.register(any())).thenReturn(userResponse);
        when(authService.refresh(any())).thenReturn(authResponse);
        when(authService.listUsers()).thenReturn(List.of(userResponse));
        when(authService.createUser(any())).thenReturn(userResponse);
        when(authService.updateRole(eq(1L), any())).thenReturn(userResponse);
        doNothing().when(authService).deleteUser(1L);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ada@example.com"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        mockMvc.perform(get("/api/v1/auth/users")
                        .header("X-User-Email", USER_EMAIL)
                        .header("X-User-Role", USER_ROLE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ada"));

        mockMvc.perform(post("/api/v1/auth/users")
                        .header("X-User-Email", USER_EMAIL)
                        .header("X-User-Role", USER_ROLE)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminUserRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mockMvc.perform(put("/api/v1/auth/users/1/role")
                        .header("X-User-Email", USER_EMAIL)
                        .header("X-User-Role", USER_ROLE)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleUpdateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mockMvc.perform(delete("/api/v1/auth/users/1")
                        .header("X-User-Email", USER_EMAIL)
                        .header("X-User-Role", USER_ROLE))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void protectedControllersCoverDonationLogisticsAndNeeds() throws Exception {
        DonationResponse donation = donationResponse(11L);
        LogisticsResponse logistics = logisticsResponse(22L);
        NeedsResponse needs = needsResponse("need-1");

        when(donationService.list()).thenReturn(List.of(donation));
        when(donationService.get(11L)).thenReturn(donation);
        when(donationService.create(any())).thenReturn(donation);
        when(donationService.update(eq(11L), any())).thenReturn(donation);
        doNothing().when(donationService).delete(11L);

        when(logisticsService.list("center-1")).thenReturn(List.of(logistics));
        when(logisticsService.create(any())).thenReturn(logistics);
        when(logisticsService.updateState(22L, "ENTREGADO")).thenReturn(logistics);

        when(needsService.list("food", "OPEN")).thenReturn(List.of(needs));
        when(needsService.get("need-1")).thenReturn(needs);
        when(needsService.create(any())).thenReturn(needs);
        when(needsService.update(eq("need-1"), any())).thenReturn(needs);
        when(needsService.receive("need-1", 5.0)).thenReturn(needs);
        doNothing().when(needsService).delete("need-1");

        mockMvc.perform(get("/api/v1/donations")
                        .header("X-User-Email", USER_EMAIL)
                        .header("X-User-Role", USER_ROLE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11));

        mockMvc.perform(get("/api/v1/donations/11")
                        .header("X-User-Email", USER_EMAIL)
                        .header("X-User-Role", USER_ROLE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11));

        mockMvc.perform(post("/api/v1/donations")
                        .header("X-User-Email", USER_EMAIL)
                        .header("X-User-Role", USER_ROLE)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(donationRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.needId").value("need-1"));

        mockMvc.perform(put("/api/v1/donations/11")
                        .header("X-User-Email", USER_EMAIL)
                        .header("X-User-Role", USER_ROLE)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(donationRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11));

        mockMvc.perform(delete("/api/v1/donations/11")
                        .header("X-User-Email", USER_EMAIL)
                        .header("X-User-Role", USER_ROLE))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/logistics")
                        .param("acopioCenterId", "center-1")
                        .header("X-User-Email", USER_EMAIL)
                        .header("X-User-Role", USER_ROLE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("EN_CAMINO"));

        mockMvc.perform(post("/api/v1/logistics")
                        .header("X-User-Email", USER_EMAIL)
                        .header("X-User-Role", USER_ROLE)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logisticsRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.donacionId").value(11));

        mockMvc.perform(put("/api/v1/logistics/22/estado")
                        .param("estado", "ENTREGADO")
                        .header("X-User-Email", USER_EMAIL)
                        .header("X-User-Role", USER_ROLE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_CAMINO"));

        mockMvc.perform(get("/api/v1/needs")
                        .param("category", "food")
                        .param("status", "OPEN")
                        .header("X-User-Email", USER_EMAIL)
                        .header("X-User-Role", USER_ROLE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("need-1"));

        mockMvc.perform(get("/api/v1/needs/need-1")
                        .header("X-User-Email", USER_EMAIL)
                        .header("X-User-Role", USER_ROLE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("need-1"));

        mockMvc.perform(post("/api/v1/needs")
                        .header("X-User-Email", USER_EMAIL)
                        .header("X-User-Role", USER_ROLE)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(needsRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.centerId").value("center-1"));

        mockMvc.perform(put("/api/v1/needs/need-1")
                        .header("X-User-Email", USER_EMAIL)
                        .header("X-User-Role", USER_ROLE)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(needsRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("need-1"));

        mockMvc.perform(put("/api/v1/needs/need-1/receive")
                        .param("amount", "5.0")
                        .header("X-User-Email", USER_EMAIL)
                        .header("X-User-Role", USER_ROLE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantityReceived").value(5.0));

        mockMvc.perform(delete("/api/v1/needs/need-1")
                        .header("X-User-Email", USER_EMAIL)
                        .header("X-User-Role", USER_ROLE))
                .andExpect(status().isNoContent());
    }





    @Test
    void serviceLayerDelegationCoversAllServiceMethods() {
        AuthClient authClient = mock(AuthClient.class);
        DonationClient donationClient = mock(DonationClient.class);
        LogisticsClient logisticsClient = mock(LogisticsClient.class);
        NeedsClient needsClient = mock(NeedsClient.class);

        AuthService authService = new AuthService(authClient);
        DonationService donationService = new DonationService(donationClient);
        LogisticsService logisticsService = new LogisticsService(logisticsClient);
        NeedsService needsService = new NeedsService(needsClient);

        AuthRequest authRequest = authRequest();
        RegisterRequest registerRequest = registerRequest();
        RefreshRequest refreshRequest = refreshRequest();
        AdminUserRequest adminUserRequest = adminUserRequest();
        RoleUpdateRequest roleUpdateRequest = roleUpdateRequest();

        AuthResponse authResponse = authResponse("access-token", "refresh-token");
        UserSummaryResponse userResponse = userResponse(2L, "Grace", "grace@example.com", "555", "USER");

        when(authClient.login(authRequest)).thenReturn(authResponse);
        when(authClient.register(registerRequest)).thenReturn(userResponse);
        when(authClient.refresh(refreshRequest)).thenReturn(authResponse);
        when(authClient.listUsers()).thenReturn(List.of(userResponse));
        when(authClient.createUser(adminUserRequest)).thenReturn(userResponse);
        when(authClient.updateRole(2L, roleUpdateRequest)).thenReturn(userResponse);

        assertSame(authResponse, authService.login(authRequest));
        assertSame(userResponse, authService.register(registerRequest));
        assertSame(authResponse, authService.refresh(refreshRequest));
        assertEquals(List.of(userResponse), authService.listUsers());
        assertSame(userResponse, authService.createUser(adminUserRequest));
        assertSame(userResponse, authService.updateRole(2L, roleUpdateRequest));
        authService.deleteUser(2L);

        DonationRequest donationRequest = donationRequest();
        DonationResponse donationResponse = donationResponse(33L);
        when(donationClient.list()).thenReturn(List.of(donationResponse));
        when(donationClient.get(33L)).thenReturn(donationResponse);
        when(donationClient.create(donationRequest)).thenReturn(donationResponse);
        when(donationClient.update(33L, donationRequest)).thenReturn(donationResponse);

        assertEquals(List.of(donationResponse), donationService.list());
        assertSame(donationResponse, donationService.get(33L));
        assertSame(donationResponse, donationService.create(donationRequest));
        assertSame(donationResponse, donationService.update(33L, donationRequest));
        donationService.delete(33L);

        LogisticsRequest logisticsRequest = logisticsRequest();
        LogisticsResponse logisticsResponse = logisticsResponse(44L);
        when(logisticsClient.list("center-2")).thenReturn(List.of(logisticsResponse));
        when(logisticsClient.create(logisticsRequest)).thenReturn(logisticsResponse);
        when(logisticsClient.updateState(44L, "ENTREGADO")).thenReturn(logisticsResponse);

        assertEquals(List.of(logisticsResponse), logisticsService.list("center-2"));
        assertSame(logisticsResponse, logisticsService.create(logisticsRequest));
        assertSame(logisticsResponse, logisticsService.updateState(44L, "ENTREGADO"));

        NeedsRequest needsRequest = needsRequest();
        NeedsResponse needsResponse = needsResponse("need-2");
        when(needsClient.list("health", "OPEN")).thenReturn(List.of(needsResponse));
        when(needsClient.get("need-2")).thenReturn(needsResponse);
        when(needsClient.create(needsRequest)).thenReturn(needsResponse);
        when(needsClient.update("need-2", needsRequest)).thenReturn(needsResponse);
        when(needsClient.receive("need-2", 9.0)).thenReturn(needsResponse);

        assertEquals(List.of(needsResponse), needsService.list("health", "OPEN"));
        assertSame(needsResponse, needsService.get("need-2"));
        assertSame(needsResponse, needsService.create(needsRequest));
        assertSame(needsResponse, needsService.update("need-2", needsRequest));
        assertSame(needsResponse, needsService.receive("need-2", 9.0));
        needsService.delete("need-2");
    }

    @Test
    void infrastructureComponentsCoverFilterInterceptorAndErrorMapping() throws Exception {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ApiError bffError = handler.handleBff(new BFFException("boom", 418)).getBody();
        assertNotNull(bffError);
        assertEquals("boom", bffError.message());
        assertEquals(418, bffError.status());

        ApiError generalError = handler.handleGeneral(new IllegalStateException("crash")).getBody();
        assertNotNull(generalError);
        assertEquals("crash", generalError.message());
        assertEquals(500, generalError.status());

        ApiError feignError = handler.handleFeign(feignException(502, "upstream down")).getBody();
        assertNotNull(feignError);
        assertEquals("upstream down", feignError.message());
        assertEquals(502, feignError.status());

        FeignAuthInterceptor interceptor = new FeignAuthInterceptor();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        USER_EMAIL,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + USER_ROLE))
                )
        );

        RequestTemplate protectedTemplate = new RequestTemplate();
        protectedTemplate.uri("/api/v1/donations");
        interceptor.apply(protectedTemplate);
        assertTrue(protectedTemplate.headers().containsKey("X-User-Email"));
        assertTrue(protectedTemplate.headers().containsKey("X-User-Role"));

        RequestTemplate publicTemplate = new RequestTemplate();
        publicTemplate.uri("/auth/login");
        interceptor.apply(publicTemplate);
        assertFalse(publicTemplate.headers().containsKey("X-User-Email"));

        SecurityContextHolder.clearContext();

        GatewayAuthFilter filter = new GatewayAuthFilter();
        FilterChain chain = mock(FilterChain.class);

        MockHttpServletRequest publicRequest = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        MockHttpServletResponse publicResponse = new MockHttpServletResponse();
        filter.doFilter(publicRequest, publicResponse, chain);
        verify(chain).doFilter(any(), any());

        Mockito.reset(chain);

        org.springframework.mock.web.MockHttpServletRequest protectedRequest = new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/donations");
        org.springframework.mock.web.MockHttpServletResponse protectedResponse = new org.springframework.mock.web.MockHttpServletResponse();
        filter.doFilter(protectedRequest, protectedResponse, chain);
        assertEquals(401, protectedResponse.getStatus());
        verifyNoInteractions(chain);

        org.springframework.mock.web.MockHttpServletRequest authorizedRequest = new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/donations");
        authorizedRequest.addHeader("X-User-Email", USER_EMAIL);
        authorizedRequest.addHeader("X-User-Role", USER_ROLE);
        org.springframework.mock.web.MockHttpServletResponse authorizedResponse = new org.springframework.mock.web.MockHttpServletResponse();
        filter.doFilter(authorizedRequest, authorizedResponse, chain);
        verify(chain).doFilter(any(), any());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());

        assertNotNull(new WebConfig().corsFilter());
    }

    private AuthRequest authRequest() {
        AuthRequest request = new AuthRequest();
        request.setEmail("user@example.com");
        request.setPassword("secret123");
        return request;
    }

    private RegisterRequest registerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Ada");
        request.setEmail("ada@example.com");
        request.setPhone("555");
        request.setPassword("secret123");
        return request;
    }

    private RefreshRequest refreshRequest() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("refresh-token");
        return request;
    }

    private AdminUserRequest adminUserRequest() {
        AdminUserRequest request = new AdminUserRequest();
        request.setName("Admin");
        request.setEmail("admin@example.com");
        request.setPhone("555");
        request.setPassword("secret123");
        request.setRole("ADMIN");
        return request;
    }

    private RoleUpdateRequest roleUpdateRequest() {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setRole("ADMIN");
        return request;
    }

    private DonationRequest donationRequest() {
        DonationRequest request = new DonationRequest();
        request.setDescripcion("Rice bags");
        request.setCantidad(10.0);
        request.setTipo("Food");
        request.setDireccion("Main street");
        request.setNeedId("need-1");
        request.setUnit("kg");
        return request;
    }

    private LogisticsRequest logisticsRequest() {
        LogisticsRequest request = new LogisticsRequest();
        request.setDonacionId(11L);
        return request;
    }

    private NeedsRequest needsRequest() {
        NeedsRequest request = new NeedsRequest();
        request.setId("need-1");
        request.setCode("CODE-1");
        request.setCategory("food");
        request.setProductName("Rice");
        request.setQuantityRequired(25.0);
        request.setQuantityReceived(5.0);
        request.setUnit("kg");
        request.setPriority("HIGH");
        request.setStatus("OPEN");
        request.setRegion("North");
        request.setCenterId("center-1");
        request.setCenterName("Center One");
        request.setAddress("Main street");
        request.setDescription("Need rice for families");
        request.setDeadline("2026-07-20");
        request.setVerifiedBy("admin@example.com");
        request.setMatchedDonations(2);
        return request;
    }

    private AuthResponse authResponse(String accessToken, String refreshToken) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setAccessExpiresIn(3600L);
        response.setRefreshExpiresIn(7200L);
        return response;
    }

    private UserSummaryResponse userResponse(Long id, String name, String email, String phone, String role) {
        UserSummaryResponse response = new UserSummaryResponse();
        response.setId(id);
        response.setName(name);
        response.setEmail(email);
        response.setPhone(phone);
        response.setRole(role);
        return response;
    }

    private DonationResponse donationResponse(Long id) {
        DonationResponse response = new DonationResponse();
        response.setId(id);
        response.setDescripcion("Rice bags");
        response.setCantidad(10.0);
        response.setTipo("Food");
        response.setDireccion("Main street");
        response.setNeedId("need-1");
        response.setDonorEmail("donor@example.com");
        response.setUnit("kg");
        response.setLogisticsId(77L);
        return response;
    }

    private LogisticsResponse logisticsResponse(Long id) {
        LogisticsResponse response = new LogisticsResponse();
        response.setId(id);
        response.setDonacionId(11L);
        response.setNeedId("need-1");
        response.setDireccion("Main street");
        response.setAcopioCenterId("center-1");
        response.setAcopioCenterName("Center One");
        response.setEstado("EN_CAMINO");
        response.setCreatedAt(Instant.parse("2026-07-06T00:00:00Z"));
        response.setAcopioDeadline(Instant.parse("2026-07-10T00:00:00Z"));
        response.setCantidadDonada(10.0);
        return response;
    }

    private NeedsResponse needsResponse(String id) {
        NeedsResponse response = new NeedsResponse();
        response.setId(id);
        response.setCode("CODE-1");
        response.setCategory("food");
        response.setProductName("Rice");
        response.setQuantityRequired(25.0);
        response.setQuantityReceived(5.0);
        response.setUnit("kg");
        response.setPriority("HIGH");
        response.setStatus("OPEN");
        response.setRegion("North");
        response.setCenterId("center-1");
        response.setCenterName("Center One");
        response.setAddress("Main street");
        response.setDescription("Need rice for families");
        response.setDeadline("2026-07-20");
        response.setCreatedAt("2026-07-01T00:00:00Z");
        response.setUpdatedAt("2026-07-02T00:00:00Z");
        response.setVerifiedBy("admin@example.com");
        response.setCreatedByEmail("creator@example.com");
        response.setMatchedDonations(2);
        return response;
    }

    private FeignException feignException(int status, String message) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/test",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                null
        );

        Response response = Response.builder()
                .status(status)
                .reason("Upstream error")
                .request(request)
                .headers(Map.of())
                .body(message, StandardCharsets.UTF_8)
                .build();

        return FeignException.errorStatus("methodKey", response);
    }
}