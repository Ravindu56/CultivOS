package com.cultivos.identity.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Login payload (#16) — identifier accepts BOTH email and +94 phone. */
public record LoginRequest(

        @NotBlank(message = "identifier is required")
        String identifier,

        @NotBlank(message = "password is required")
        String password
) {
}
