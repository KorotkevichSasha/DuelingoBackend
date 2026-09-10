--liquibase formatted sql

--changeset duelrush:2026-08-25-learning-micro-rewards
ALTER TABLE users ADD COLUMN last_daily_tip_reward_at DATE;
ALTER TABLE users ADD COLUMN listening_reward_date DATE;
ALTER TABLE users ADD COLUMN listening_gold_today INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users ADD CONSTRAINT ck_users_listening_gold_today CHECK (listening_gold_today BETWEEN 0 AND 15);

--changeset duelrush:2026-08-25-achievement-copy
UPDATE achievements
SET title = CASE required_value
        WHEN 1 THEN 'Первый шаг навстречу'
        WHEN 2 THEN 'Начало компании'
        WHEN 5 THEN 'Собираем друзей'
        WHEN 10 THEN 'Связующее звено'
        ELSE 'Душа сообщества'
    END,
    description = 'Начните знакомство с ' || required_value || ' игроками, которые приняли вашу заявку',
    updated_at = CURRENT_TIMESTAMP
WHERE type = 'INVITES';

--changeset duelrush:2026-08-25-community-showcase splitStatements:true
--validCheckSum: 1:any
DELETE FROM users WHERE lower(email) LIKE '%@example.com';

INSERT INTO users (
    id, username, password, email, role, points, avatar_url, email_verified,
    gold, rush_charges, rush_charges_updated_at, virtual_player, highest_league_rewarded
)
SELECT v.id::uuid, v.username,
       '$2a$10$U3tGZ.IsUQwf4D4v4Z.4QO2e2jJZsAM2q3k7pB5vQ7bB6s1YzYdW2',
       v.email, 'USER', v.points, 'default:' || v.avatar, TRUE,
       100 + (v.points / 20), 10, CURRENT_TIMESTAMP, FALSE, 0
FROM (VALUES
    ('a1000000-0000-4000-8000-000000000001','ArtemK','artem.k@demo.duelrush.app',85,1),
    ('a1000000-0000-4000-8000-000000000002','MashaLupikova','masha.l@demo.duelrush.app',210,2),
    ('a1000000-0000-4000-8000-000000000003','VladEnglish','vlad.e@demo.duelrush.app',335,3),
    ('a1000000-0000-4000-8000-000000000004','KiraMoon','kira.m@demo.duelrush.app',470,4),
    ('a1000000-0000-4000-8000-000000000005','MaximPro','maxim.p@demo.duelrush.app',620,5),
    ('a1000000-0000-4000-8000-000000000006','DashaReads','dasha.r@demo.duelrush.app',790,6),
    ('a1000000-0000-4000-8000-000000000007','NikitaFox','nikita.f@demo.duelrush.app',940,7),
    ('a1000000-0000-4000-8000-000000000008','AlinaSky','alina.s@demo.duelrush.app',1120,8),
    ('a1000000-0000-4000-8000-000000000009','EgorMinsk','egor.m@demo.duelrush.app',1290,9),
    ('a1000000-0000-4000-8000-000000000010','SofiaBright','sofia.b@demo.duelrush.app',1510,10),
    ('a1000000-0000-4000-8000-000000000011','RomanWave','roman.w@demo.duelrush.app',1680,1),
    ('a1000000-0000-4000-8000-000000000012','LeraNova','lera.n@demo.duelrush.app',1970,2),
    ('a1000000-0000-4000-8000-000000000013','TimurStorm','timur.s@demo.duelrush.app',2210,3),
    ('a1000000-0000-4000-8000-000000000014','PolinaArt','polina.a@demo.duelrush.app',2470,4),
    ('a1000000-0000-4000-8000-000000000015','AntonNorth','anton.n@demo.duelrush.app',2710,5),
    ('a1000000-0000-4000-8000-000000000016','EvaSunrise','eva.s@demo.duelrush.app',3020,6),
    ('a1000000-0000-4000-8000-000000000017','GlebFocus','gleb.f@demo.duelrush.app',3260,7),
    ('a1000000-0000-4000-8000-000000000018','AnyaDream','anya.d@demo.duelrush.app',3660,8),
    ('a1000000-0000-4000-8000-000000000019','IlyaVector','ilya.v@demo.duelrush.app',4010,9),
    ('a1000000-0000-4000-8000-000000000020','KatyaFree','katya.f@demo.duelrush.app',4450,10)
) AS v(id, username, email, points, avatar)
ON CONFLICT DO NOTHING;
