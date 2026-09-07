package com.cultivos.identity.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Registration payload — #14. Sri Lankan E.164 mobile and platform languages enforced. */
public record RegisterRequest(

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid address")
        String email,

        @NotBlank(message = "phone is required")
        @Pattern(regexp = "^\\+94[0-9]{9}$", message = "phone must be Sri Lankan E.164 format (+94XXXXXXXXX)")
        String phone,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 72, message = "password must be at least 8 characters")
        String password,

        @NotBlank(message = "fullName is required")
        @Size(max = 120, message = "fullName must be at most 120 characters")
        String fullName,

        @Pattern(regexp = "^(en|si|ta)$", message = "preferredLanguage must be en, si, or ta")
        String preferredLanguage
) {
}
