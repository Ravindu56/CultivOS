package com.cultivos.identity.auth;

import com.cultivos.identity.auth.dto.LoginRequest;
import com.cultivos.identity.auth.dto.RefreshRequest;
import com.cultivos.identity.auth.dto.RegisterRequest;
import com.cultivos.identity.auth.dto.RegisterResponse;
import com.cultivos.identity.auth.dto.TokenResponse;
import com.cultivos.identity.security.JwtService;
import com.cultivos.identity.user.Role;
import com.cultivos.identity.user.RoleRepository;
import com.cultivos.identity.user.User;
import com.cultivos.identity.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    /**
     * BCrypt hash of a throwaway password. When the login identifier is unknown we still
     * run {@code matches()} against this, so response timing does not reveal account existence (#16).
     */
    private static final String DUMMY_HASH = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** Registers a new account with the default FARMER role (#14). */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("email");
        }
        if (userRepository.existsByPhone(request.phone())) {
            throw new DuplicateUserException("phone");
        }

        Role farmer = roleRepository.findByName(Role.FARMER)
                .orElseThrow(() -> new IllegalStateException(
                        "FARMER role missing — verify Flyway V1 seed data"));

        User user = new User(
                request.email(),
                request.phone(),
                passwordEncoder.encode(request.password()),
                request.fullName(),
                request.preferredLanguage());
        user.getRoles().add(farmer);

        User saved = userRepository.save(user);

        return new RegisterResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getFullName(),
                saved.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
    }

    /** Login with email OR +94 phone (#16). Wrong password and unknown identifier fail identically. */
    public TokenResponse login(LoginRequest request) {
        User user = userRepository
                .findByEmailOrPhone(request.identifier(), request.identifier())
                .orElse(null);

        // Always run BCrypt — identical 401 and near-identical timing on both failure paths
        String hashToCheck = user != null ? user.getPasswordHash() : DUMMY_HASH;
        boolean passwordMatches = passwordEncoder.matches(request.password(), hashToCheck);
        if (user == null || !passwordMatches) {
            throw new InvalidCredentialsException();
        }

        return TokenResponse.of(
                jwtService.issueAccessToken(user.getId(), user.getEmail(), roleNames(user)),
                jwtService.issueRefreshToken(user.getId()),
                jwtService.accessTokenTtlSeconds());
    }

    /** Refresh: validates the type=refresh claim before issuing a new access token (#16). */
    public TokenResponse refresh(RefreshRequest request) {
        final Claims claims;
        try {
            claims = jwtService.parse(request.refreshToken());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidRefreshTokenException();
        }
        if (!JwtService.TYPE_REFRESH.equals(claims.get(JwtService.CLAIM_TYPE, String.class))) {
            throw new InvalidRefreshTokenException();
        }

        User user = userRepository.findById(UUID.fromString(claims.getSubject()))
                .orElseThrow(InvalidRefreshTokenException::new);

        return TokenResponse.of(
                jwtService.issueAccessToken(user.getId(), user.getEmail(), roleNames(user)),
                request.refreshToken(),
                jwtService.accessTokenTtlSeconds());
    }

    private static List<String> roleNames(User user) {
        return user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
    }
}
