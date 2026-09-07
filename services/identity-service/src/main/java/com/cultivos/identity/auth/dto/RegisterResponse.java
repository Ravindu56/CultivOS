package com.cultivos.identity.auth.dto;

import java.util.Set;
import java.util.UUID;

/** 201 response for registration — never contains the password or its hash. */
public record RegisterResponse(
        UUID userId,
        String email,
        String fullName,
        Set<String> roles
) {
}
