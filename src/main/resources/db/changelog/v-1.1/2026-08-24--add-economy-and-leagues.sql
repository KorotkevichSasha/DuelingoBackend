-- liquibase formatted sql

-- changeset duelrush:2026-08-24-add-player-economy
ALTER TABLE users ADD COLUMN gold INTEGER NOT NULL DEFAULT 100;
ALTER TABLE users ADD COLUMN rush_charges INTEGER NOT NULL DEFAULT 25;
ALTER TABLE users ADD COLUMN rush_charges_updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN virtual_player BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE users ADD CONSTRAINT ck_users_gold_non_negative CHECK (gold >= 0);
ALTER TABLE users ADD CONSTRAINT ck_users_rush_charges_range CHECK (rush_charges BETWEEN 0 AND 25);

UPDATE users
SET virtual_player = TRUE,
    gold = 0,
    rush_charges = 25
WHERE email LIKE '%@players.duelrush.app';

-- changeset duelrush:2026-08-24-add-duel-economy-metadata
ALTER TABLE duel ADD COLUMN ranked BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE duel ADD COLUMN difficulty VARCHAR(16) NOT NULL DEFAULT 'MEDIUM';
ALTER TABLE duel ADD COLUMN rewards_settled BOOLEAN NOT NULL DEFAULT FALSE;
