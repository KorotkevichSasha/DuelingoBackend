--liquibase formatted sql

--changeset codex:2026-07-31-remove-demo-accounts
DELETE FROM users
WHERE email IN (
    'duelmaster@example.com', 'quizking@example.com', 'wordwarrior@example.com',
    'ninja@example.com', 'expert@example.com', 'polyglot@example.com',
    'fastlearner@example.com', 'wordsmith@example.com', 'syntax@example.com',
    'viking@example.com', 'phantom@example.com', 'guru@example.com',
    'leo@example.com', 'wizard@example.com', 'edu@example.com', 'topplayer@example.com'
);

--changeset codex:2026-07-31-clean-achievement-content
UPDATE achievements
SET title = CASE
        WHEN type = 'DUELS' AND level = 'BRONZE' THEN 'Duel beginner'
        WHEN type = 'DUELS' AND level = 'SILVER' THEN 'Experienced duelist'
        WHEN type = 'DUELS' AND level = 'GOLD' THEN 'Duel master'
        WHEN type = 'FRIENDS' AND level = 'BRONZE' THEN 'Friendly learner'
        WHEN type = 'FRIENDS' AND level = 'SILVER' THEN 'Community builder'
        WHEN type = 'FRIENDS' AND level = 'GOLD' THEN 'Social star'
        WHEN type = 'TESTS' AND level = 'BRONZE' THEN 'Student'
        WHEN type = 'TESTS' AND level = 'SILVER' THEN 'Skilled student'
        WHEN type = 'TESTS' AND level = 'GOLD' THEN 'Test expert'
        WHEN type = 'WORDS' AND level = 'BRONZE' THEN 'Word collector'
        WHEN type = 'WORDS' AND level = 'SILVER' THEN 'Vocabulary builder'
        WHEN type = 'WORDS' AND level = 'GOLD' THEN 'Lexicon expert'
        ELSE title
    END,
    description = CASE type
        WHEN 'DUELS' THEN 'Play ' || required_value || ' duels'
        WHEN 'FRIENDS' THEN 'Add ' || required_value || ' friends'
        WHEN 'TESTS' THEN 'Complete ' || required_value || ' tests'
        WHEN 'WORDS' THEN 'Add ' || required_value || ' words'
        ELSE description
    END,
    icon_url = NULL,
    updated_at = CURRENT_TIMESTAMP;

--changeset codex:2026-07-31-user-data-constraints
ALTER TABLE users ADD CONSTRAINT chk_users_points_nonnegative CHECK (points >= 0);
ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'));
