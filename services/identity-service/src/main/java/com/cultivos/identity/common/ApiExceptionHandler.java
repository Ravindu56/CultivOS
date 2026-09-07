package com.cultivos.identity.common;

import com.cultivos.identity.auth.DuplicateUserException;
import com.cultivos.identity.auth.InvalidCredentialsException;
import com.cultivos.identity.auth.InvalidRefreshTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/** Uniform error bodies: 400 field errors, 401 auth failures, 409 duplicates. */
@RestControllerAdvice
public class ApiExceptionHandler {

    public record FieldError(String field, String message) {
    }

    public record ApiError(int status, String message, List<FieldError> errors, Instant timestamp) {
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateUserException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(409, ex.getMessage(),
                        List.of(new FieldError(ex.getField(), ex.getMessage())), Instant.now()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError(401, ex.getMessage(), List.of(), Instant.now()));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiError> handleInvalidRefresh(InvalidRefreshTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError(401, ex.getMessage(), List.of(), Instant.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest()
                .body(new ApiError(400, "Validation failed", errors, Instant.now()));
    }
}
