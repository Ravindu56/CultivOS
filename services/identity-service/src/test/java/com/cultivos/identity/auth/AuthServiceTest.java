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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String PASSWORD = "securePass1";

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtService jwtService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, roleRepository, passwordEncoder, jwtService);
        lenient().when(jwtService.issueAccessToken(any(), anyString(), anyCollection())).thenReturn("access-token");
        lenient().when(jwtService.issueRefreshToken(any())).thenReturn("refresh-token");
        lenient().when(jwtService.accessTokenTtlSeconds()).thenReturn(900L);
    }

    // ---- #14 register ----

    @Test
    void registerSuccessHashesPasswordAndAssignsFarmerRole() {
        RegisterRequest request = new RegisterRequest(
                "nimal@example.lk", "+94771234567", PASSWORD, "Nimal Perera", "si");
        when(userRepository.existsByEmail("nimal@example.lk")).thenReturn(false);
        when(userRepository.existsByPhone("+94771234567")).thenReturn(false);
        when(roleRepository.findByName(Role.FARMER)).thenReturn(Optional.of(new Role(Role.FARMER)));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponse response = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User persisted = captor.getValue();
        assertThat(persisted.getPasswordHash()).startsWith("$2a$").isNotEqualTo(PASSWORD);
        assertThat(persisted.getRoles()).extracting(Role::getName).containsExactly("FARMER");
        assertThat(persisted.getPreferredLanguage()).isEqualTo("si");
        assertThat(response.email()).isEqualTo("nimal@example.lk");
        assertThat(response.roles()).containsExactly("FARMER");
    }

    @Test
    void registerDefaultsPreferredLanguageToEnglish() {
        RegisterRequest request = new RegisterRequest(
                "kumar@example.lk", "+94779876543", PASSWORD, "Kumar S", null);
        when(userRepository.existsByEmail("kumar@example.lk")).thenReturn(false);
        when(userRepository.existsByPhone("+94779876543")).thenReturn(false);
        when(roleRepository.findByName(Role.FARMER)).thenReturn(Optional.of(new Role(Role.FARMER)));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPreferredLanguage()).isEqualTo("en");
    }

    @Test
    void duplicateEmailThrowsConflict() {
        RegisterRequest request = new RegisterRequest(
                "nimal@example.lk", "+94771234567", PASSWORD, "Nimal Perera", null);
        when(userRepository.existsByEmail("nimal@example.lk")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessageContaining("email");
        verify(userRepository, never()).save(any());
    }

    @Test
    void duplicatePhoneThrowsConflict() {
        RegisterRequest request = new RegisterRequest(
                "nimal@example.lk", "+94771234567", PASSWORD, "Nimal Perera", null);
        when(userRepository.existsByEmail("nimal@example.lk")).thenReturn(false);
        when(userRepository.existsByPhone("+94771234567")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessageContaining("phone");
        verify(userRepository, never()).save(any());
    }

    // ---- #16 login ----

    @Test
    void loginWithEmailReturnsTokenPair() {
        User user = persistedUser();
        when(userRepository.findByEmailOrPhone("nimal@example.lk", "nimal@example.lk"))
                .thenReturn(Optional.of(user));

        TokenResponse response = authService.login(new LoginRequest("nimal@example.lk", PASSWORD));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900L);
    }

    @Test
    void loginWithPhoneReturnsTokenPair() {
        User user = persistedUser();
        when(userRepository.findByEmailOrPhone("+94771234567", "+94771234567"))
                .thenReturn(Optional.of(user));

        TokenResponse response = authService.login(new LoginRequest("+94771234567", PASSWORD));

        assertThat(response.accessToken()).isEqualTo("access-token");
    }

    @Test
    void loginWrongPasswordThrowsGeneric401() {
        User user = persistedUser();
        when(userRepository.findByEmailOrPhone("nimal@example.lk", "nimal@example.lk"))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("nimal@example.lk", "wrongPass9")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void loginUnknownIdentifierThrowsIdenticalGeneric401() {
        when(userRepository.findByEmailOrPhone("ghost@example.lk", "ghost@example.lk"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("ghost@example.lk", PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials"); // identical to the wrong-password message
    }

    // ---- #16 refresh ----

    @Test
    void refreshValidTokenIssuesNewAccessToken() {
        User user = persistedUser();
        Claims claims = mock(Claims.class);
        when(claims.get(JwtService.CLAIM_TYPE, String.class)).thenReturn(JwtService.TYPE_REFRESH);
        when(claims.getSubject()).thenReturn(user.getId().toString());
        when(jwtService.parse("old-refresh")).thenReturn(claims);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        TokenResponse response = authService.refresh(new RefreshRequest("old-refresh"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("old-refresh");
        verify(jwtService).issueAccessToken(eq(user.getId()), eq(user.getEmail()), anyCollection());
    }

    @Test
    void refreshRejectsAccessToken() {
        Claims claims = mock(Claims.class);
        when(claims.get(JwtService.CLAIM_TYPE, String.class)).thenReturn(null); // access tokens carry no type
        when(jwtService.parse("access-token-as-refresh")).thenReturn(claims);

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest("access-token-as-refresh")))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void refreshRejectsInvalidToken() {
        when(jwtService.parse("garbage")).thenThrow(new JwtException("bad"));

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest("garbage")))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    // ---- helpers ----

    private User persistedUser() {
        User user = new User("nimal@example.lk", "+94771234567",
                passwordEncoder.encode(PASSWORD), "Nimal Perera", "en");
        user.getRoles().add(new Role(Role.FARMER));
        return user;
    }
}
