-- CultivOS local dev: one database per microservice (database-per-service pattern).
-- Runs automatically on first Postgres container boot (docker-entrypoint-initdb.d).

CREATE DATABASE cultivos_identity;
CREATE DATABASE cultivos_field_registry;
CREATE DATABASE cultivos_ai_crop;
CREATE DATABASE cultivos_cultivation;
CREATE DATABASE cultivos_resource;
CREATE DATABASE cultivos_marketplace;
CREATE DATABASE cultivos_notification;
CREATE DATABASE cultivos_payment;
CREATE DATABASE cultivos_gis;
CREATE DATABASE cultivos_reporting;
CREATE DATABASE cultivos_user_profile;
CREATE DATABASE cultivos_admin;

-- PostGIS where spatial data lives
\c cultivos_field_registry
CREATE EXTENSION IF NOT EXISTS postgis;

\c cultivos_gis
CREATE EXTENSION IF NOT EXISTS postgis;
