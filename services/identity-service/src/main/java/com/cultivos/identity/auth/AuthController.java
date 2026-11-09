package com.cultivos.identity.auth;

import com.cultivos.identity.auth.dto.LoginRequest;
import com.cultivos.identity.auth.dto.MeResponse;
import com.cultivos.identity.auth.dto.RefreshRequest;
import com.cultivos.identity.auth.dto.RegisterRequest;
import com.cultivos.identity.auth.dto.RegisterResponse;
import com.cultivos.identity.auth.dto.TokenResponse;
import com.cultivos.identity.security.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    /** #17 — returns the current caller's JWT claims; used by the #18 integration test. */
    @GetMapping("/me")
    @SuppressWarnings("unchecked")
    public MeResponse me(@AuthenticationPrincipal Claims claims) {
        return new MeResponse(
                claims.getSubject(),
                claims.get("email", String.class),
                claims.get(JwtService.CLAIM_ROLES, List.class));
    }

    /** #17 — temporary RBAC probe; removed once real role-gated endpoints exist. */
    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Map<String, String> adminOnly() {
        return Map.of("status", "ok");
    }
}
