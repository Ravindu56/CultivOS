# ADR-003: Kafka KRaft Mode — Zookeeper Removed

**Status:** Accepted · **Date:** 2026-08-30 · **Epic:** #21

## Context
The event backbone (cultivation events, notifications, audit logs) runs on Apache Kafka. The original compose used Confluent Kafka 7.6 + Zookeeper. Kafka 4.0 (Mar 2025) removed Zookeeper entirely; 4.3.1 (Jun 2026) is the current release.

## Decision
- Apache Kafka 4.3.x in KRaft mode — single-node broker+controller for local dev; Strimzi operator manages Kafka on Kubernetes.
- Dual listeners in dev: `localhost:9092` for host processes, `kafka:29092` for in-network containers (Kafka UI).
- Zookeeper is removed from all environments.

## Consequences
- Positive: one less stateful system; faster broker startup (~17s healthy in local validation); simpler K8s manifests via Strimzi.
- Negative: KRaft dual-listener config is a common stumbling block for new contributors — documented in the compose file comments.
