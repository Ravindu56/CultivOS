# ADR-001: WSO2 Identity Server as Primary IAM

**Status:** Accepted · **Date:** 2026-08-30 · **Epic:** #21

## Context
CultivOS needs national-scale CIAM: 8 user roles, SMS OTP for farmers (phone-first, no email), OAuth2/OIDC JWT issuance (RS256), and alignment with Sri Lankan government identity practices. Candidates: WSO2 Identity Server 7.1.0 vs Keycloak 26.7.x. Validation on `develop` (#22) also surfaced that WSO2 IS ships a self-signed TLS certificate on :9443, which browsers and JVMs reject by default.

## Decision
- WSO2 Identity Server 7.1.0 is the primary IAM — OIDC issuer, RS256 tokens, 8 roles seeded, OTP connector.
- Keycloak 26.7.x is retained as a lightweight local-dev fallback profile only — never a parallel implementation.
- All services validate JWTs against a configurable issuer URL — zero vendor coupling; IS and Keycloak are swappable.
- TLS: export the IS public certificate into a shared Java truststore mounted into identity-service (and other JVM callers) in every environment. Relaxed-SSL HTTP clients are rejected outside throwaway spikes.

## Consequences
- Positive: enterprise CIAM features (OTP, federation, consent), government familiarity, commercial support path for national rollout.
- Negative: heavier container and longer cold start (~90s) than Keycloak; truststore setup required for JVM service-to-IS calls.
- Mitigation: IS runs in staging/prod-like environments; Keycloak profile keeps the local dev loop fast.
