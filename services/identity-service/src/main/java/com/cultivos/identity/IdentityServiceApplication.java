package com.cultivos.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CultivOS Identity Service.
 *
 * Security backbone of the platform: user registration, login,
 * JWT (RS256) access/refresh tokens, and RBAC for all 8 platform roles.
 * Every other CultivOS service validates tokens issued here.
 */
@SpringBootApplication
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
