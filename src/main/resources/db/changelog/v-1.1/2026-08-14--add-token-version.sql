-- liquibase formatted sql
-- changeset duelrush:2026-08-14-add-token-version
ALTER TABLE users ADD COLUMN token_version INTEGER NOT NULL DEFAULT 0;
