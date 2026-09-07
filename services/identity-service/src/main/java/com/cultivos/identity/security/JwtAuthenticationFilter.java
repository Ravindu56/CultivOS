package com.cultivos.identity.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * #17 — verifies Bearer JWTs and populates the SecurityContext.
 *
 * <p>Rules:
 * <ul>
 *   <li>Access tokens authenticate the request (roles claim → {@code ROLE_*} authorities)</li>
 *   <li>Refresh tokens are NEVER accepted as API credentials — no context is set</li>
 *   <li>Invalid/expired tokens leave the context empty; the entry point answers 401</li>
 *   <li>Tokens and passwords are never logged</li>
 * </ul>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.parse(header.substring(7));
                boolean isRefresh = JwtService.TYPE_REFRESH.equals(
                        claims.get(JwtService.CLAIM_TYPE, String.class));
                if (!isRefresh) {
                    List<String> roles = claims.get(JwtService.CLAIM_ROLES, List.class);
                    List<SimpleGrantedAuthority> authorities = roles == null
                            ? List.of()
                            : roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
                    var authentication = new UsernamePasswordAuthenticationToken(claims, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | IllegalArgumentException e) {
                log.debug("Rejected JWT: {}", e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }
}
