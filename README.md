# CultivOS 🌾
### Sri Lanka's Modular Open Source Agriculture Platform (MOSAP)

> AI-driven crop recommendations · GIS field mapping · Open marketplace · Farmer–officer coordination · Seed & input management · National analytics

[![CI — Java Services](https://github.com/Ravindu56/CultivOS/actions/workflows/ci-java-services.yml/badge.svg)](https://github.com/Ravindu56/CultivOS/actions/workflows/ci-java-services.yml)
[![CI — AI Crop Service](https://github.com/Ravindu56/CultivOS/actions/workflows/ci-python-service.yml/badge.svg)](https://github.com/Ravindu56/CultivOS/actions/workflows/ci-python-service.yml)

## Tech Stack

| Layer | Technology |
|---|---|
| Core microservices | Spring Boot (Java 21) |
| AI service | Python 3.11 · FastAPI · Scikit-learn / TensorFlow |
| Mobile app | Flutter |
| Web portal | React.js + TypeScript |
| Message broker | Apache Kafka |
| Primary database | PostgreSQL + PostGIS |
| Cache | Redis |
| Deployment | Kubernetes + Helm |
| CI/CD | GitHub Actions + ArgoCD |

## Repository Layout (Monorepo)

```
services/          12 backend microservices (11 Spring Boot + 1 FastAPI)
apps/mobile-app    Flutter app (Farmer, Field Officer, Trader, …)
apps/web-portal    React.js + TypeScript portal
infra/             Helm charts, K8s manifests, DB init scripts
docs/              ADRs, API specs, guides
.github/           CI workflows, issue & PR templates
```

## Microservices & Local Ports

| # | Service | Port | Stack |
|---|---|---|---|
| — | api-gateway | 8000 | Spring Cloud Gateway |
| 1 | identity-service | 8081 | Spring Boot |
| 2 | field-registry-service | 8082 | Spring Boot |
| 3 | ai-crop-service | 8083 | Python FastAPI |
| 4 | cultivation-service | 8084 | Spring Boot |
| 5 | resource-service | 8085 | Spring Boot |
| 6 | marketplace-service | 8086 | Spring Boot |
| 7 | notification-service | 8087 | Spring Boot |
| 8 | payment-service | 8088 | Spring Boot |
| 9 | gis-service | 8089 | Spring Boot |
| 10 | reporting-service | 8090 | Spring Boot |
| 11 | user-profile-service | 8091 | Spring Boot |
| 12 | admin-service | 8092 | Spring Boot |

## Quick Start

```bash
docker compose -f docker-compose.dev.yml up -d   # PostGIS, Redis, Kafka, Kafka UI
```

Full guide: [docs/getting-started.md](docs/getting-started.md)

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Branch from `develop`, PR back to `develop`, conventional commits.

## License

MIT — see [LICENSE](LICENSE)
