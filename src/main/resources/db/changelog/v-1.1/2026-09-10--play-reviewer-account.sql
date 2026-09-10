--liquibase formatted sql

--changeset duelrush:2026-09-10-play-reviewer-account
UPDATE users
SET password = '$2b$10$h9YKZ.5ESH2z2bZ4m65TZ.F5qHLfDlae7qCJRjKzTr7ZeSz54rYQC',
    email_verified = TRUE,
    role = 'USER',
    token_version = token_version + 1
WHERE lower(username) = lower('PlayReviewer')
   OR lower(email) = lower('duelrush.app+playreview@gmail.com');

INSERT INTO users (
    id, username, password, email, role, points, avatar_url, email_verified,
    gold, rush_charges, rush_charges_updated_at, virtual_player,
    highest_league_rewarded, token_version
)
SELECT 'a2000000-0000-4000-8000-000000000001'::uuid,
       'PlayReviewer',
       '$2b$10$h9YKZ.5ESH2z2bZ4m65TZ.F5qHLfDlae7qCJRjKzTr7ZeSz54rYQC',
       'duelrush.app+playreview@gmail.com',
       'USER', 180, 'default:3', TRUE,
       250, 10, CURRENT_TIMESTAMP, FALSE, 0, 0
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE lower(username) = lower('PlayReviewer')
       OR lower(email) = lower('duelrush.app+playreview@gmail.com')
);
