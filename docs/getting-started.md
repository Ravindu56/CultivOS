# Getting Started with CultivOS

## Prerequisites

- Java 21 (Temurin recommended)
- Python 3.11+
- Docker Engine + Compose v2
- Flutter 3.x
- Node.js 20+
- Git + GitHub CLI (`gh`)

## Local Development

### 1. Start the infrastructure

```bash
docker compose -f docker-compose.dev.yml up -d
```

Starts:
- PostgreSQL 16 + PostGIS on `5432` (user `cultivos`, password `cultivos_dev`)
- Redis 7 on `6379`
- Kafka on `9092`
- Kafka UI on http://localhost:8080

On first boot, `infra/db/init/01-create-databases.sql` creates one database per microservice (database-per-service pattern).

### 2. Verify infrastructure

```bash
docker compose -f docker-compose.dev.yml ps
psql postgresql://cultivos:cultivos_dev@localhost:5432/cultivos_db -c '\l'
```

### 3. Run a service

```bash
cd services/identity-service
./mvnw spring-boot:run
```

## Service Ports

| Service | Port |
|---|---|
| api-gateway | 8000 |
| identity-service | 8081 |
| field-registry-service | 8082 |
| ai-crop-service | 8083 |
| cultivation-service | 8084 |
| resource-service | 8085 |
| marketplace-service | 8086 |
| notification-service | 8087 |
| payment-service | 8088 |
| gis-service | 8089 |
| reporting-service | 8090 |
| user-profile-service | 8091 |
| admin-service | 8092 |

## Core Kafka Topics

| Topic | Producer | Consumers |
|---|---|---|
| `user.registered` | identity-service | notification-service |
| `field.registered` | field-registry-service | ai-crop-service |
| `crop.recommended` | ai-crop-service | cultivation-service |
| `order.placed` | marketplace-service | payment-service, notification-service |
| `payment.completed` | payment-service | marketplace-service, notification-service |

## Branch Naming

```
feature/STORY-001-user-registration
hotfix/BUG-042-jwt-expiry
release/v1.0.0
```
