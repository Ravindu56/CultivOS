package com.cultivos.identity.auth;

/** Refresh failure (#16) — invalid, expired, wrong-type token, or deleted user. */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Invalid or expired refresh token");
    }
}
