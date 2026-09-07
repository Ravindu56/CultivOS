package com.cultivos.identity.auth;

/**
 * Login failure (#16) — one generic message for BOTH wrong password and unknown
 * identifier, so the API never reveals which accounts exist.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
