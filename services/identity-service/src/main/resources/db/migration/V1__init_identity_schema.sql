-- V1 — identity schema: roles, users, user_roles + seed 8 platform roles
-- Issue #10 · Epic #3 — Flyway owns the schema; Hibernate runs in validate mode.

-- Extensions ------------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS citext;      -- case-insensitive email matching
CREATE EXTENSION IF NOT EXISTS pgcrypto;    -- gen_random_uuid() (core since PG13; kept for safety)

-- Roles -----------------------------------------------------------------------
CREATE TABLE roles (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(30) UNIQUE NOT NULL
);

-- Users -----------------------------------------------------------------------
CREATE TABLE users (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email              CITEXT UNIQUE NOT NULL,
    phone              VARCHAR(12) UNIQUE NOT NULL
                       CONSTRAINT users_phone_lk_chk
                       CHECK (phone ~ '^\+94[0-9]{9}$'),       -- E.164, Sri Lanka: +94XXXXXXXXX
    password_hash      VARCHAR(72) NOT NULL,                    -- BCrypt $2a$… (60 chars + headroom)
    full_name          VARCHAR(120) NOT NULL,
    preferred_language VARCHAR(2) NOT NULL DEFAULT 'en'
                       CONSTRAINT users_preferred_language_chk
                       CHECK (preferred_language IN ('en', 'si', 'ta')),
    status             VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- NOTE: UNIQUE constraints on email/phone already create the backing B-tree
-- indexes, satisfying the #10 index requirement without duplicate indexes.

-- User ↔ Role join -------------------------------------------------------------
CREATE TABLE user_roles (
    user_id UUID   NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Seed: the 8 CultivOS platform roles ------------------------------------------
INSERT INTO roles (name) VALUES
    ('FARMER'),
    ('FIELD_OFFICER'),
    ('REGIONAL_MANAGER'),
    ('TRADER'),
    ('RETAILER'),
    ('CONSUMER'),
    ('SUPPLIER'),
    ('SUPER_ADMIN');
