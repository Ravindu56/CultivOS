# ADR-002: WSO2 API Manager as API Gateway

**Status:** Accepted (Phase 2 adoption) · **Date:** 2026-08-30 · **Epic:** #21

## Context
CultivOS needs a unified entry point with per-role rate limiting (farmers 100 req/min, traders 200 req/min, anonymous 20 req/min), JWT validation, and — from Phase 2 — a developer portal exposing marketplace APIs to external traders and government consumers. Candidates: WSO2 API Manager 4.5.0, Apache APISIX, Spring Cloud Gateway (already scaffolded).

## Decision
- WSO2 API Manager 4.5.0 is the target API management layer, adopted in Phase 2 when marketplace APIs go external.
- Spring Cloud Gateway remains the internal routing layer until then; it is fronted or replaced by APIM at adoption.
- Apache APISIX is the documented fallback if APIM proves too heavy.
- A Phase 1 spike (APIM gateway fronting identity-service routes) de-risks the adoption.

## Consequences
- Positive: full API lifecycle management, developer portal, monetization tiers for trader APIs, analytics.
- Negative: another JVM platform to operate; gateway logic must be portable (no business logic in any gateway layer).
