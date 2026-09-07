package com.cultivos.identity.auth;

/** Thrown when email or phone is already registered — mapped to 409 Conflict. */
public class DuplicateUserException extends RuntimeException {

    private final String field;

    public DuplicateUserException(String field) {
        super("An account with this " + field + " already exists");
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
