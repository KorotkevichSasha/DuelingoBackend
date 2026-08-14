-- liquibase formatted sql
-- changeset duelrush:2026-08-10-add-email-verification
ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT TRUE;
