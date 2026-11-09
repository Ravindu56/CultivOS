# CultivOS 🌾
### Sri Lanka's Modular Open Source Agriculture Platform (MOSAP)

> AI-driven crop recommendations · GIS field mapping · Open marketplace · Farmer–officer coordination · Seed & input management · National analytics

[![CI — Java Services](https://github.com/Ravindu56/CultivOS/actions/workflows/ci-java-services.yml/badge.svg)](https://github.com/Ravindu56/CultivOS/actions/workflows/ci-java-services.yml)
[![CI — AI Crop Service](https://github.com/Ravindu56/CultivOS/actions/workflows/ci-python-service.yml/badge.svg)](https://github.com/Ravindu56/CultivOS/actions/workflows/ci-python-service.yml)

CultivOS is a cloud-native, microservice-based agricultural operating system engineered for Sri Lanka's agrarian sector. It unifies field intelligence, agrarian coordination (GN divisions), resource allocation, and direct farmer-to-buyer commerce into a resilient, production-ready digital public infrastructure.

---

## 🛠️ Technology Stack (Enterprise OSS Baseline v1.1)

| Layer | Primary Technology | Fallback / Dev Profile | Key Capabilities & Role in CultivOS |
|---|---|---|---|
| **Identity & Access (IAM)** | **WSO2 Identity Server 7.1.0** | Keycloak 26.7.x | Multi-role OIDC/JWT issuing, RBAC, OTP authentication |
| **API Gateway** | **WSO2 API Manager 4.5.0** | Spring Cloud Gateway / APISIX | Unified API entrypoint, rate limiting, traffic routing, developer portal |
| **Enterprise Integration** | **WSO2 Micro Integrator 4.4.0** | Apache Camel | Integration bridges for Department of Agriculture (DoA) & CROPIX |
| **Core Microservices** | **Spring Boot (Java 21)** | — | Business logic, domain services, Kafka event producers/consumers |
| **AI & MLOps** | **Python 3.11 · FastAPI · MLflow 3.x** | Triton Inference Server | Crop suitability recommendations, yield prediction, model lifecycle |
| **Geospatial (GIS)** | **GeoServer + PostGIS 3.5** | QGIS Server | OGC WMS/WFS map services, parcel boundaries, spatial queries |
| **Event Streaming** | **Apache Kafka 4.3.x (KRaft)** | Strimzi Operator | Asynchronous event choreography, audit events, decoupled domain events |
| **Persistence & Cache** | **PostgreSQL 17+ · Redis 7.x** | — | Relational store per service, distributed caching, session state |
| **Object Storage** | **MinIO** | AWS S3 | S3-compatible storage for crop imagery, certificates, and soil reports |
| **Search & Analytics** | **OpenSearch 3.x** | — | Full-text search for marketplace listings, aggregate analytics, audit logs |
| **Client Applications** | **Flutter (Mobile) · React.js + TS (Web)** | — | Multi-role apps (Farmer, Officer, Trader) & Administrative Portal |
| **Observability** | **OpenTelemetry + Prometheus + Grafana** | Jaeger | Distributed tracing, APM, metrics scraping, cluster monitoring |
| **Infrastructure & CI/CD** | **Kubernetes (1.35+) · Helm · ArgoCD** | GitHub Actions | Declarative GitOps deployment and automated container pipelines |

---

## 🏛️ Microservices & Local Architecture

The platform follows a modular microservice architecture orchestrated via Kubernetes:

| # | Service | Default Port | Technology | Primary Function |
|---|---|---|---|---|
| — | `api-gateway` | 8000 | WSO2 APIM / Spring Gateway | Reverse proxy, rate limiting, unified routing |
| 1 | `identity-service` | 8081 | Spring Boot / WSO2 IS | Token validation adapter, profile lifecycle, RBAC |
| 2 | `field-registry-service` | 8082 | Spring Boot + PostGIS | Field mapping, GN division tagging, ownership |
| 3 | `ai-crop-service` | 8083 | Python FastAPI + Scikit/TF | Crop suitability scoring & localized agronomic AI |
| 4 | `cultivation-service` | 8084 | Spring Boot | Crop cycles, harvesting schedules, seasonal tracking |
| 5 | `resource-service` | 8085 | Spring Boot | Fertilizer, seed quotas, subsidy distribution |
| 6 | `marketplace-service` | 8086 | Spring Boot + OpenSearch | Farmer-to-buyer crop listing, bidding, orders |
| 7 | `notification-service` | 8087 | Spring Boot | Push notifications, localized SMS fallback (Gov SMS) |
| 8 | `payment-service` | 8088 | Spring Boot | PayHere, eZCash, mCash integration & escrows |
| 9 | `gis-service` | 8089 | Spring Boot + GeoServer | GeoJSON/WFS layer rendering, satellite/drone overlays |
| 10 | `reporting-service` | 8090 | Spring Boot | Regional harvest forecasts, macro agricultural stats |
| 11 | `user-profile-service` | 8091 | Spring Boot | Farmer, Field Officer, and Buyer credential metadata |
| 12 | `admin-service` | 8092 | Spring Boot | Platform configuration, master data, system auditing |

---

## 📁 Repository Layout (Monorepo)

```
services/               12 backend microservices (11 Spring Boot + 1 FastAPI)
apps/mobile-app         Flutter app (Farmer, Field Officer, Trader, Consumer)
apps/web-portal         React.js + TypeScript administration & analytics portal
infra/
  ├── docker/           Docker Compose setups (dev profiles, Kafka KRaft, MinIO)
  ├── helm/             Helm charts for CultivOS microservices
  └── k8s/              Kubernetes manifests and ArgoCD application configs
docs/
  ├── adr/              Architectural Decision Records (ADR-001 through ADR-005)
  ├── api/              OpenAPI / Swagger specifications
  └── guides/           Local onboarding & deployment guides
.github/                GitHub Actions CI workflows, issue and PR templates
```

---

## 🚀 Quick Start (Local Development)

Start the local backing services (PostgreSQL/PostGIS, Redis, Kafka KRaft, MinIO):

```bash
docker compose -f docker-compose.dev.yml up -d
```

For identity federation in local dev, activate the Keycloak/WSO2 lightweight profile:
```bash
docker compose -f docker-compose.dev.yml --profile auth up -d
```

Detailed onboarding and environment setup: [docs/getting-started.md](docs/getting-started.md)

---

## 🤝 Contributing

We follow a GitFlow-inspired trunk-based branch model:
- Branch feature branches from `develop` (`feature/<issue-id>-<short-description>`).
- Submit Pull Requests back against `develop`.
- Ensure all commits follow the [Conventional Commits](https://www.conventionalcommits.org/) standard.
- Details in [CONTRIBUTING.md](CONTRIBUTING.md).

## 📄 License

Licensed under the [MIT License](LICENSE).
