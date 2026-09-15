-- liquibase formatted sql
-- changeset duelrush:2026-09-15-google-play-billing
CREATE TABLE play_purchases (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    purchase_token_hash VARCHAR(64) NOT NULL UNIQUE,
    product_id VARCHAR(80) NOT NULL,
    gold_awarded INTEGER NOT NULL CHECK (gold_awarded > 0),
    purchased_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_play_purchases_user ON play_purchases(user_id, purchased_at DESC);
