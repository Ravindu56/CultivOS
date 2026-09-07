package com.cultivos.identity.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Issues and verifies RS256 JWTs (#11).
 *
 * <p>Access token (15 min): {@code sub} (userId), {@code email}, {@code roles[]} ,
 * {@code iss=cultivos-identity}, {@code iat}, {@code exp}.
 * Refresh token (7 days): {@code sub}, {@code type=refresh}, {@code exp}.
 *
 * <p>Keys are RSA PEM files loaded from configurable paths — never committed to git.
 * Other services verify tokens using ONLY the public key; keep claims stable.
 */
@Service
public class JwtService {

    public static final String ISSUER = "cultivos-identity";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_TYPE = "type";
    public static final String TYPE_REFRESH = "refresh";

    private final Resource privateKeyResource;
    private final Resource publicKeyResource;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    public JwtService(
            @Value("${jwt.private-key-path}") Resource privateKeyResource,
            @Value("${jwt.public-key-path}") Resource publicKeyResource,
            @Value("${jwt.access-token-ttl:PT15M}") Duration accessTokenTtl,
            @Value("${jwt.refresh-token-ttl:P7D}") Duration refreshTokenTtl) {
        this.privateKeyResource = privateKeyResource;
        this.publicKeyResource = publicKeyResource;
        this.accessTokenTtl = accessTokenTtl;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    @PostConstruct
    void loadKeys() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.privateKey = (RSAPrivateKey) keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(pemBytes(privateKeyResource, "PRIVATE KEY")));
            this.publicKey = (RSAPublicKey) keyFactory.generatePublic(
                    new X509EncodedKeySpec(pemBytes(publicKeyResource, "PUBLIC KEY")));
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Failed to load JWT RSA keypair. Generate dev keys with openssl "
                            + "(PKCS#8 private key) and check jwt.private-key-path / jwt.public-key-path.", e);
        }
    }

    /** Issues a signed access token: sub, email, roles[], iss, iat, exp. */
    public String issueAccessToken(UUID userId, String email, Collection<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuer(ISSUER)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtl)))
                .claim("email", email)
                .claim(CLAIM_ROLES, List.copyOf(roles))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /** Issues a signed refresh token: sub, type=refresh, exp. */
    public String issueRefreshToken(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuer(ISSUER)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshTokenTtl)))
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /**
     * Parses and verifies a token against the public key.
     * Expired/invalid tokens throw {@code JwtException} — mapped to 401 by the security filter (#17).
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .requireIssuer(ISSUER)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Public key accessor — other services will fetch this (or a JWKS endpoint) to verify tokens. */
    public RSAPublicKey publicKey() {
        return publicKey;
    }

    private static byte[] pemBytes(Resource resource, String blockType) throws IOException {
        String pem = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
                .replace("-----BEGIN " + blockType + "-----", "")
                .replace("-----END " + blockType + "-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(pem);
    }
}
