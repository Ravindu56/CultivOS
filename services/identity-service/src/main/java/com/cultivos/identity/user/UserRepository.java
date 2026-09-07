package com.cultivos.identity.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    /** Login lookup (#16) — pass the same identifier twice; CITEXT keeps email case-insensitive. */
    Optional<User> findByEmailOrPhone(String email, String phone);
}
