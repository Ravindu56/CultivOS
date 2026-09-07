package com.cultivos.identity.auth;

import com.cultivos.identity.auth.dto.RegisterRequest;
import com.cultivos.identity.auth.dto.RegisterResponse;
import com.cultivos.identity.user.Role;
import com.cultivos.identity.user.RoleRepository;
import com.cultivos.identity.user.User;
import com.cultivos.identity.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
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
}
