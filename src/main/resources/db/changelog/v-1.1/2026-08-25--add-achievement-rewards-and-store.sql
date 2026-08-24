--liquibase formatted sql

--changeset duelrush:add-achievement-rewards-and-store splitStatements:true
ALTER TABLE achievements ADD COLUMN reward_gold INTEGER NOT NULL DEFAULT 0;
ALTER TABLE achievements ADD CONSTRAINT ck_achievements_reward_gold_non_negative CHECK (reward_gold >= 0);

UPDATE achievements
SET reward_gold = CASE level
    WHEN 'BRONZE' THEN 15
    WHEN 'SILVER' THEN 40
    WHEN 'GOLD' THEN 90
    ELSE 10
END;

ALTER TABLE user_achievements ADD COLUMN reward_claimed BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE user_achievements ADD COLUMN reward_claimed_at TIMESTAMP WITHOUT TIME ZONE;

INSERT INTO achievements (id, title, description, type, level, required_value, reward_gold, condition_type, icon_url)
VALUES
    ('d5000000-0000-4000-8000-000000000001', 'Первый приглашённый', 'Пригласите первого друга в свою команду', 'INVITES', 'BRONZE', 1, 20, 'FRIEND_INVITED', NULL),
    ('d5000000-0000-4000-8000-000000000002', 'Дуэт собран', 'Пригласите 2 друзей в свою команду', 'INVITES', 'BRONZE', 2, 30, 'FRIEND_INVITED', NULL),
    ('d5000000-0000-4000-8000-000000000003', 'Своя команда', 'Пригласите 5 друзей в свою команду', 'INVITES', 'BRONZE', 5, 55, 'FRIEND_INVITED', NULL),
    ('d5000000-0000-4000-8000-000000000004', 'Капитан клуба', 'Пригласите 10 друзей в свою команду', 'INVITES', 'SILVER', 10, 90, 'FRIEND_INVITED', NULL),
    ('d5000000-0000-4000-8000-000000000005', 'Основатель сообщества', 'Пригласите 25 друзей в свою команду', 'INVITES', 'GOLD', 25, 160, 'FRIEND_INVITED', NULL);

INSERT INTO user_achievements (
    id, user_id, achievement_id, current_value, is_achieved, achieved_at, reward_claimed
)
SELECT
    gen_random_uuid(),
    invited.user_id,
    achievement.id,
    LEAST(invited.invite_count, achievement.required_value),
    invited.invite_count >= achievement.required_value,
    CASE WHEN invited.invite_count >= achievement.required_value THEN CURRENT_TIMESTAMP ELSE NULL END,
    FALSE
FROM achievements achievement
CROSS JOIN (
    SELECT from_user_id AS user_id, COUNT(*)::INTEGER AS invite_count
    FROM user_relationship
    WHERE status = 'FRIEND'
    GROUP BY from_user_id
) invited
WHERE achievement.condition_type = 'FRIEND_INVITED'
ON CONFLICT (user_id, achievement_id) DO NOTHING;

ALTER TABLE users DROP CONSTRAINT IF EXISTS ck_users_rush_charges_range;
UPDATE users
SET rush_charges = LEAST(rush_charges, 10),
    rush_charges_updated_at = CASE WHEN rush_charges > 10 THEN CURRENT_TIMESTAMP ELSE rush_charges_updated_at END;
ALTER TABLE users ALTER COLUMN rush_charges SET DEFAULT 10;
ALTER TABLE users ADD CONSTRAINT ck_users_rush_charges_range CHECK (rush_charges BETWEEN 0 AND 10);
