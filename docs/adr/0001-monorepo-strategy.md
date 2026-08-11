# ADR-0001: Monorepo Strategy for CultivOS

- **Status:** Accepted
- **Date:** 2026-08-11

## Context

CultivOS comprises 12 microservices, a Flutter mobile app, a React web portal, and shared infrastructure. We must choose between polyrepo (one repo per service) and a monorepo.

## Decision

Use a single monorepo: `Ravindu56/CultivOS`. Path-filtered GitHub Actions (dorny/paths-filter + dynamic matrix) ensure only changed services build, test, and publish images.

## Consequences

**Positive**
- Atomic cross-service changes in one PR
- One issue tracker, one Project board, one milestone set
- Shared CI workflow templates and issue/PR templates
- Simplified onboarding: one clone, one `docker compose up`

**Negative / Mitigations**
- CI cost risk without filtering → mitigated by affected-only execution
- Repo-wide access → add CODEOWNERS as the team grows
