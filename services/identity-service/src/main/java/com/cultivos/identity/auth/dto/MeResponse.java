package com.cultivos.identity.auth.dto;

import java.util.List;

/** Current-callers-claims response for GET /api/v1/auth/me (#17). */
public record MeResponse(
        String userId,
        String email,
        List<String> roles
) {
}
