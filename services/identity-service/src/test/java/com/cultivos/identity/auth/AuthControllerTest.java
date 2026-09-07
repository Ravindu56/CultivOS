package com.cultivos.identity.auth;

import com.cultivos.identity.auth.dto.LoginRequest;
import com.cultivos.identity.auth.dto.RefreshRequest;
import com.cultivos.identity.auth.dto.RegisterRequest;
import com.cultivos.identity.auth.dto.RegisterResponse;
import com.cultivos.identity.auth.dto.TokenResponse;
import com.cultivos.identity.config.SecurityConfig;
import com.cultivos.identity.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    // ---- #14 register ----

    @Test
    void validRequestReturns201WithoutPasswordMaterial() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "nimal@example.lk", "+94771234567", "securePass1", "Nimal Perera", "si");
        when(authService.register(any())).thenReturn(new RegisterResponse(
                UUID.randomUUID(), "nimal@example.lk", "Nimal Perera", Set.of("FARMER")));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value("nimal@example.lk"))
                .andExpect(jsonPath("$.roles[0]").value("FARMER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void invalidPayloadReturns400WithFieldErrors() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "not-an-email", "0771234567", "short", "", "de");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.length()").value(5));
    }

    @Test
    void duplicateAccountReturns409() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "nimal@example.lk", "+94771234567", "securePass1", "Nimal Perera", null);
        when(authService.register(any())).thenThrow(new DuplicateUserException("email"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0].field").value("email"));
    }

    // ---- #16 login + refresh ----

    @Test
    void loginReturnsTokenPair() throws Exception {
        when(authService.login(any())).thenReturn(TokenResponse.of("access", "refresh", 900L));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("+94771234567", "securePass1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void loginFailureReturnsGeneric401() throws Exception {
        when(authService.login(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("ghost@example.lk", "whatever1"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void refreshReturnsNewAccessToken() throws Exception {
        when(authService.refresh(any())).thenReturn(TokenResponse.of("new-access", "old-refresh", 900L));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest("old-refresh"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"));
    }

    @Test
    void refreshFailureReturns401() throws Exception {
        when(authService.refresh(any())).thenThrow(new InvalidRefreshTokenException());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest("garbage"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired refresh token"));
    }

    // ---- #17 filter chain + RBAC ----

    @Test
    void meWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meWithValidAccessTokenReturnsClaims() throws Exception {
        when(jwtService.parse("good-token")).thenReturn(claimsWith(null, List.of("FARMER")));

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer good-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("nimal@example.lk"))
                .andExpect(jsonPath("$.roles[0]").value("FARMER"));
    }

    @Test
    void refreshTokenIsNotAcceptedForApiAccess() throws Exception {
        when(jwtService.parse("refresh-token"))
                .thenReturn(claimsWith(JwtService.TYPE_REFRESH, null));

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer refresh-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminOnlyRejectsFarmerRoleWith403() throws Exception {
        when(jwtService.parse("farmer-token")).thenReturn(claimsWith(null, List.of("FARMER")));

        mockMvc.perform(get("/api/v1/auth/admin-only").header("Authorization", "Bearer farmer-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminOnlyAcceptsSuperAdmin() throws Exception {
        when(jwtService.parse("admin-token")).thenReturn(claimsWith(null, List.of("SUPER_ADMIN")));

        mockMvc.perform(get("/api/v1/auth/admin-only").header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    private static Claims claimsWith(String type, List<String> roles) {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(claims.get("email", String.class)).thenReturn("nimal@example.lk");
        when(claims.get(JwtService.CLAIM_TYPE, String.class)).thenReturn(type);
        when(claims.get(JwtService.CLAIM_ROLES, List.class)).thenReturn(roles);
        return claims;
    }
}
