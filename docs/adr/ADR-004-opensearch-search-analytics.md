# ADR-004: OpenSearch for Search & Analytics

**Status:** Accepted (Phase 2 adoption) · **Date:** 2026-08-30 · **Epic:** #21

## Context
Phase 2 marketplace needs full-text search across produce listings (crop, variety, price, district) and the platform needs audit-log aggregation. Candidates: OpenSearch vs Elasticsearch. Elasticsearch's SSPL/Elastic License is not OSI-approved; OpenSearch is Apache 2.0.

## Decision
- OpenSearch 3.x for marketplace search and audit-log aggregation, adopted in Phase 2.
- Deployed via the OpenSearch Kubernetes operator; index-per-domain (listings, audit) with ISM retention policies.

## Consequences
- Positive: license-safe for an open-source national platform; mature K8s operator; covers both search and log analytics.
- Negative: slightly smaller plugin ecosystem than Elastic; team must learn OpenSearch Dashboards for ops visibility.
