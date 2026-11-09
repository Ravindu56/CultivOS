package com.cultivos.identity.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Refresh payload (#16). */
public record RefreshRequest(

        @NotBlank(message = "refreshToken is required")
        String refreshToken
) {
}
