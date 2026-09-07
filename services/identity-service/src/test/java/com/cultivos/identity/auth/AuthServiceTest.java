package com.cultivos.identity.auth;

import com.cultivos.identity.auth.dto.RegisterRequest;
import com.cultivos.identity.auth.dto.RegisterResponse;
import com.cultivos.identity.user.Role;
import com.cultivos.identity.user.RoleRepository;
import com.cultivos.identity.user.User;
import com.cultivos.identity.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, roleRepository, new BCryptPasswordEncoder(10));
    }

    @Test
    void registerSuccessHashesPasswordAndAssignsFarmerRole() {
        RegisterRequest request = new RegisterRequest(
                "nimal@example.lk", "+94771234567", "securePass1", "Nimal Perera", "si");
        when(userRepository.existsByEmail("nimal@example.lk")).thenReturn(false);
        when(userRepository.existsByPhone("+94771234567")).thenReturn(false);
        when(roleRepository.findByName(Role.FARMER)).thenReturn(Optional.of(new Role(Role.FARMER)));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponse response = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User persisted = captor.getValue();
        assertThat(persisted.getPasswordHash()).startsWith("$2a$").isNotEqualTo("securePass1");
        assertThat(persisted.getRoles()).extracting(Role::getName).containsExactly("FARMER");
        assertThat(persisted.getPreferredLanguage()).isEqualTo("si");
        assertThat(response.email()).isEqualTo("nimal@example.lk");
        assertThat(response.fullName()).isEqualTo("Nimal Perera");
        assertThat(response.roles()).containsExactly("FARMER");
    }

    @Test
    void registerDefaultsPreferredLanguageToEnglish() {
        RegisterRequest request = new RegisterRequest(
                "kumar@example.lk", "+94779876543", "securePass1", "Kumar S", null);
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
                "nimal@example.lk", "+94771234567", "securePass1", "Nimal Perera", null);
        when(userRepository.existsByEmail("nimal@example.lk")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessageContaining("email");
        verify(userRepository, never()).save(any());
    }

    @Test
    void duplicatePhoneThrowsConflict() {
        RegisterRequest request = new RegisterRequest(
                "nimal@example.lk", "+94771234567", "securePass1", "Nimal Perera", null);
        when(userRepository.existsByEmail("nimal@example.lk")).thenReturn(false);
        when(userRepository.existsByPhone("+94771234567")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessageContaining("phone");
        verify(userRepository, never()).save(any());
    }
}
