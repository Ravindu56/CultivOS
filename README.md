# CultivOS 🌾
### Sri Lanka's Modular Open Source Agriculture Platform (MOSAP)

> AI-driven crop recommendations · GIS field mapping · Open marketplace · Farmer–officer coordination · Seed & input management · National analytics

[![CI — Java Services](https://github.com/Ravindu56/CultivOS/actions/workflows/ci-java-services.yml/badge.svg)](https://github.com/Ravindu56/CultivOS/actions/workflows/ci-java-services.yml)
[![CI — AI Crop Service](https://github.com/Ravindu56/CultivOS/actions/workflows/ci-python-service.yml/badge.svg)](https://github.com/Ravindu56/CultivOS/actions/workflows/ci-python-service.yml)

CultivOS is a cloud-native, microservice-based agricultural operating system for Sri Lanka. It unifies field intelligence, farmer coordination (GN divisions), resource management, and direct farmer-to-buyer trading in one platform.

**Status legend:** ✅ Built · 🚧 In progress · 📋 Planned

> CultivOS is under active development. The stack below is the v1.0 target (WSO2-first). The Status column shows what exists in this repository today.

---

## Technology Stack (v1.0 target)

| Layer | Primary | Fallback / Dev profile | Status |
|---|---|---|---|
| Identity & Access | WSO2 Identity Server 7.1.0 | Keycloak 26.7.x | 🚧 Container in compose; identity-service still issues its own RS256 JWTs. Migration to WSO2 IS planned for Phase 1 |
| API Gateway | WSO2 API Manager 4.5.0 | Apache APISIX / Spring Cloud Gateway | 📋 Planned (Phase 2) |
| Integration | WSO2 Micro Integrator 4.4.0 | Apache Camel | 📋 Planned (Phase 3, CROPIX / DoA) |
| Core services | Spring Boot (Java 21) | — | 🚧 identity-service implemented; other services are placeholders |
| AI service | Python 3.11 · FastAPI · Scikit-learn / TensorFlow | — | 🚧 Dependency manifests and CI only |
| MLOps & serving | MLflow 3.x + Triton Inference Server | — | 📋 Planned (Phase 3) |
| Geospatial | GeoServer + PostGIS 3.5 | QGIS Server | 🚧 PostGIS 17-3.5 in compose; GeoServer planned (Phase 2) |
| Message broker | Apache Kafka 4.3.x (KRaft) + Strimzi | — | 🚧 Kafka 4.3.1 KRaft in compose; Strimzi planned for Kubernetes |
| Data storage | PostgreSQL 17+ · Redis 7 · MinIO | — | ✅ Defined in `docker-compose.dev.yml` |
| Search & analytics | OpenSearch 3.x | — | 📋 Planned (Phase 2) |
| Observability | OpenTelemetry · Prometheus · Grafana | Jaeger | 🚧 OTel collector in compose; Prometheus and Grafana planned |
| Mobile app | Flutter | — | 📋 Planned (placeholder directory) |
| Web portal | React.js + TypeScript | — | 📋 Planned (placeholder directory) |
| Deployment | Kubernetes · Helm · ArgoCD | — | 📋 Planned (`infra/helm` and `infra/k8s` are placeholders) |
| CI | GitHub Actions | — | ✅ Java and Python workflows present |

Decisions are recorded in [docs/adr](docs/adr) (ADR-001 to ADR-005 cover WSO2 IS, WSO2 APIM, Kafka KRaft, OpenSearch, and WSO2 Micro Integrator).

---

## Microservices & Local Ports

| # | Service | Port | Stack | Status |
|---|---|---|---|---|
| — | api-gateway | 8000 | WSO2 APIM (fallback: Spring Cloud Gateway) | 📋 Planned |
| 1 | identity-service | 8081 | Spring Boot | 🚧 Register, login, refresh, JWT filter, RBAC, Flyway with 8 role seeds. Moving to WSO2 IS |
| 2 | field-registry-service | 8082 | Spring Boot + PostGIS | 📋 Planned |
| 3 | ai-crop-service | 8083 | Python FastAPI | 🚧 Scaffold |
| 4 | cultivation-service | 8084 | Spring Boot | 📋 Planned |
| 5 | resource-service | 8085 | Spring Boot | 📋 Planned |
| 6 | marketplace-service | 8086 | Spring Boot + OpenSearch | 📋 Planned |
| 7 | notification-service | 8087 | Spring Boot | 📋 Planned |
| 8 | payment-service | 8088 | Spring Boot | 📋 Planned |
| 9 | gis-service | 8089 | Spring Boot + GeoServer | 📋 Planned |
| 10 | reporting-service | 8090 | Spring Boot | 📋 Planned |
| 11 | user-profile-service | 8091 | Spring Boot | 📋 Planned |
| 12 | admin-service | 8092 | Spring Boot | 📋 Planned |

---

## Phased Roadmap

| Phase | Scope | Key components |
|---|---|---|
| Phase 1 | Core registration + AI recommendations | WSO2 IS, identity, field registry, AI crop service, Kafka, PostGIS |
| Phase 2 | Marketplace + GIS | WSO2 APIM, marketplace, payments, GIS, GeoServer, OpenSearch |
| Phase 3 | Advanced analytics + full AI | WSO2 Micro Integrator (CROPIX), MLflow + Triton, reporting, dashboards |

---

## Repository Layout (Monorepo)

```
services/          12 backend microservices (11 Spring Boot + 1 FastAPI)
apps/mobile-app    Flutter app (placeholder)
apps/web-portal    React.js + TypeScript portal (placeholder)
infra/db           PostgreSQL init scripts
infra/otel         OpenTelemetry collector config
infra/helm         Helm charts (placeholder)
infra/k8s          Kubernetes manifests (placeholder)
docs/adr           Architecture Decision Records
docs/api           API specs (placeholder)
.github/           CI workflows, issue & PR templates
```

---

## Quick Start

```bash
docker compose -f docker-compose.dev.yml up -d
```

This starts PostgreSQL + PostGIS, Redis, Kafka 4.3 (KRaft), Kafka UI, MinIO, a WSO2 Identity Server 7.1.0 container, and an OpenTelemetry collector. WSO2 IS is not yet wired to any service, and compose profiles are not yet defined.

Full guide: [docs/getting-started.md](docs/getting-started.md)

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Branch from `develop`, PR back to `develop`, conventional commits.

## License

MIT — see [LICENSE](LICENSE)
