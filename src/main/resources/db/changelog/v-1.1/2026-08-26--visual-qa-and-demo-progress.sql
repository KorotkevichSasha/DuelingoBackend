--liquibase formatted sql

--changeset duelrush:2026-08-26-friend-duel-achievements
UPDATE achievements
SET title = CASE required_value
        WHEN 1 THEN 'Первый бой плечом к плечу'
        WHEN 2 THEN 'Дружеский реванш'
        WHEN 5 THEN 'Проверенная команда'
        WHEN 10 THEN 'Боевые товарищи'
        ELSE 'Легенды дружеских дуэлей'
    END,
    description = 'Завершите ' || required_value || ' дуэлей с друзьями',
    condition_type = 'FRIEND_DUEL_PLAYED',
    updated_at = CURRENT_TIMESTAMP
WHERE type = 'INVITES';

UPDATE user_achievements
SET current_value = 0, is_achieved = FALSE, achieved_at = NULL, reward_claimed = FALSE, reward_claimed_at = NULL
WHERE achievement_id IN (SELECT id FROM achievements WHERE type = 'INVITES');

--changeset duelrush:2026-08-26-poetitp-social-showcase splitStatements:true
UPDATE users
SET password = '$2a$10$xLctmdk3XmnDAMdlzW5Fm.5bw9lHp03/HXnm9eCuQmNM00Par5p1K'
WHERE lower(username) = 'poetitp';

INSERT INTO user_relationship (id, from_user_id, to_user_id, status)
SELECT gen_random_uuid(), poet.id, demo.id, 'FRIEND'::user_relationship_status_type
FROM users poet
JOIN users demo ON demo.id IN (
    'a1000000-0000-4000-8000-000000000001'::uuid,
    'a1000000-0000-4000-8000-000000000002'::uuid,
    'a1000000-0000-4000-8000-000000000003'::uuid,
    'a1000000-0000-4000-8000-000000000004'::uuid,
    'a1000000-0000-4000-8000-000000000005'::uuid,
    'a1000000-0000-4000-8000-000000000006'::uuid,
    'a1000000-0000-4000-8000-000000000007'::uuid,
    'a1000000-0000-4000-8000-000000000008'::uuid
)
WHERE lower(poet.username) = 'poetitp'
  AND NOT EXISTS (
      SELECT 1 FROM user_relationship r
      WHERE (r.from_user_id = poet.id AND r.to_user_id = demo.id)
         OR (r.from_user_id = demo.id AND r.to_user_id = poet.id)
  );

INSERT INTO user_relationship (id, from_user_id, to_user_id, status)
SELECT gen_random_uuid(), poet.id, demo.id, 'FRIEND_REQUEST'::user_relationship_status_type
FROM users poet
JOIN users demo ON demo.id IN (
    'a1000000-0000-4000-8000-000000000009'::uuid,
    'a1000000-0000-4000-8000-000000000010'::uuid,
    'a1000000-0000-4000-8000-000000000011'::uuid,
    'a1000000-0000-4000-8000-000000000012'::uuid
)
WHERE lower(poet.username) = 'poetitp'
  AND NOT EXISTS (SELECT 1 FROM user_relationship r WHERE (r.from_user_id = poet.id AND r.to_user_id = demo.id) OR (r.from_user_id = demo.id AND r.to_user_id = poet.id));

INSERT INTO user_relationship (id, from_user_id, to_user_id, status)
SELECT gen_random_uuid(), demo.id, poet.id, 'FRIEND_REQUEST'::user_relationship_status_type
FROM users poet
JOIN users demo ON demo.id IN (
    'a1000000-0000-4000-8000-000000000013'::uuid,
    'a1000000-0000-4000-8000-000000000014'::uuid,
    'a1000000-0000-4000-8000-000000000015'::uuid,
    'a1000000-0000-4000-8000-000000000016'::uuid
)
WHERE lower(poet.username) = 'poetitp'
  AND NOT EXISTS (SELECT 1 FROM user_relationship r WHERE (r.from_user_id = poet.id AND r.to_user_id = demo.id) OR (r.from_user_id = demo.id AND r.to_user_id = poet.id));

INSERT INTO user_achievements (id, user_id, achievement_id, current_value, is_achieved, achieved_at, reward_claimed)
SELECT gen_random_uuid(), poet.id, achievement.id,
       LEAST(8, achievement.required_value), 8 >= achievement.required_value,
       CASE WHEN 8 >= achievement.required_value THEN CURRENT_TIMESTAMP ELSE NULL END, FALSE
FROM users poet CROSS JOIN achievements achievement
WHERE lower(poet.username) = 'poetitp' AND achievement.condition_type = 'FRIEND_ADDED'
ON CONFLICT (user_id, achievement_id) DO UPDATE
SET current_value = EXCLUDED.current_value,
    is_achieved = EXCLUDED.is_achieved,
    achieved_at = EXCLUDED.achieved_at;
