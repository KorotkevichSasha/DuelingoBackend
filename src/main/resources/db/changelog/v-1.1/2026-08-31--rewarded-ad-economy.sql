-- liquibase formatted sql

-- changeset duelrush:2026-08-31-rewarded-ad-economy
ALTER TABLE users ADD COLUMN rewarded_ad_date DATE;
ALTER TABLE users ADD COLUMN rewarded_ads_today INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN last_rewarded_ad_at TIMESTAMP;
ALTER TABLE users ADD CONSTRAINT ck_users_rewarded_ads_today
    CHECK (rewarded_ads_today BETWEEN 0 AND 5);
