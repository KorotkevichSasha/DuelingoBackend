-- liquibase formatted sql

-- changeset duelrush:2026-08-25-add-league-promotion-rewards
ALTER TABLE users ADD COLUMN highest_league_rewarded INTEGER NOT NULL DEFAULT 0;

UPDATE users
SET highest_league_rewarded = CASE
    WHEN points >= 3650 THEN 9
    WHEN points >= 3000 THEN 8
    WHEN points >= 2450 THEN 7
    WHEN points >= 1950 THEN 6
    WHEN points >= 1500 THEN 5
    WHEN points >= 1100 THEN 4
    WHEN points >= 750 THEN 3
    WHEN points >= 450 THEN 2
    WHEN points >= 200 THEN 1
    ELSE 0
END;

ALTER TABLE users ADD CONSTRAINT ck_users_highest_league_rewarded
    CHECK (highest_league_rewarded BETWEEN 0 AND 9);
