# ADR-005: WSO2 Micro Integrator for Government Integration

**Status:** Accepted (Phase 3 adoption) · **Date:** 2026-08-30 · **Epic:** #21

## Context
Phase 3 requires interoperability with government systems — CROPIX (agriculture DPI) and Department of Agriculture services — involving legacy protocols (SOAP/XML), batch ETL, and file-based data drops. Candidates: WSO2 Micro Integrator 4.4.0 vs Apache Camel.

## Decision
- WSO2 Micro Integrator 4.4.0 is the integration runtime for government systems, adopted in Phase 3.
- Apache Camel (Camel K) is the documented fallback.
- Until Phase 3, inter-service integration stays Kafka choreography + synchronous REST/gRPC — no ESB in the internal critical path.

## Consequences
- Positive: purpose-built protocol bridging (SOAP→REST), connectors, and a government-familiar integration stack matching the WSO2 IAM/APIM choices.
- Negative: deferred adoption means integration requirements must be captured early (Phase 2) to avoid rework; MI adds a config-driven runtime the team must learn.
