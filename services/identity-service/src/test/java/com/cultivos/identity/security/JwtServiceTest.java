package com.cultivos.identity.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Unit tests for {@link JwtService} — ephemeral RSA keypair per test run, no files committed. */
class JwtServiceTest {

    @TempDir
    Path tempDir;

    private Path privateKeyPath;
    private Path publicKeyPath;
    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        writeKeypair(tempDir, "id");
        privateKeyPath = tempDir.resolve("id-private.pem");
        publicKeyPath = tempDir.resolve("id-public.pem");
        jwtService = buildService(Duration.ofMinutes(15), Duration.ofDays(7));
    }

    @Test
    void accessTokenVerifiesWithPublicKeyAndCarriesClaims() {
        UUID userId = UUID.randomUUID();

        String token = jwtService.issueAccessToken(userId, "farmer@cultivos.lk", List.of("FARMER"));
        Claims claims = jwtService.parse(token);

        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.getIssuer()).isEqualTo(JwtService.ISSUER);
        assertThat(claims.get("email", String.class)).isEqualTo("farmer@cultivos.lk");
        assertThat(claims.get(JwtService.CLAIM_ROLES, List.class)).containsExactly("FARMER");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void rolesClaimIsAJsonArray() {
        String token = jwtService.issueAccessToken(
                UUID.randomUUID(), "admin@cultivos.lk", List.of("SUPER_ADMIN", "REGIONAL_MANAGER"));

        Claims claims = jwtService.parse(token);

        assertThat(claims.get(JwtService.CLAIM_ROLES, List.class))
                .containsExactly("SUPER_ADMIN", "REGIONAL_MANAGER");
    }

    @Test
    void refreshTokenMarksTypeClaimAndOmitsRoles() {
        String token = jwtService.issueRefreshToken(UUID.randomUUID());

        Claims claims = jwtService.parse(token);

        assertThat(claims.get(JwtService.CLAIM_TYPE, String.class)).isEqualTo(JwtService.TYPE_REFRESH);
        assertThat(claims.get(JwtService.CLAIM_ROLES)).isNull();
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService shortLived = buildService(Duration.ofSeconds(-60), Duration.ofSeconds(-60));
        String token = shortLived.issueAccessToken(UUID.randomUUID(), "a@b.lk", List.of("FARMER"));

        assertThatThrownBy(() -> shortLived.parse(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tokenSignedWithForeignKeyIsRejected() throws Exception {
        writeKeypair(tempDir, "foreign");
        JwtService foreign = new JwtService(
                new FileSystemResource(tempDir.resolve("foreign-private.pem")),
                new FileSystemResource(tempDir.resolve("foreign-public.pem")),
                Duration.ofMinutes(15), Duration.ofDays(7));
        foreign.loadKeys();
        String foreignToken = foreign.issueAccessToken(UUID.randomUUID(), "x@y.lk", List.of("FARMER"));

        assertThatThrownBy(() -> jwtService.parse(foreignToken)).isInstanceOf(SignatureException.class);
    }

    private JwtService buildService(Duration accessTtl, Duration refreshTtl) {
        JwtService service = new JwtService(
                new FileSystemResource(privateKeyPath),
                new FileSystemResource(publicKeyPath),
                accessTtl, refreshTtl);
        service.loadKeys();
        return service;
    }

    private static void writeKeypair(Path dir, String prefix) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        Files.writeString(dir.resolve(prefix + "-private.pem"),
                toPem("PRIVATE KEY", keyPair.getPrivate().getEncoded()));
        Files.writeString(dir.resolve(prefix + "-public.pem"),
                toPem("PUBLIC KEY", keyPair.getPublic().getEncoded()));
    }

    private static String toPem(String type, byte[] encoded) {
        return "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(encoded)
                + "\n-----END " + type + "-----\n";
    }
}
