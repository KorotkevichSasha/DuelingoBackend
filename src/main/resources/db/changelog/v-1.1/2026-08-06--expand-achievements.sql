--liquibase formatted sql

--changeset codex:expand-achievements splitStatements:true
INSERT INTO achievements (id, title, description, type, level, required_value, condition_type, icon_url)
SELECT v.id::uuid, v.title, v.description, v.type, v.level, v.required_value, v.condition_type, NULL
FROM (VALUES
    ('d1000000-0000-4000-8000-000000000001','Первый вызов','Завершите свою первую дуэль','DUELS','BRONZE',1,'DUEL_PLAYED'),
    ('d1000000-0000-4000-8000-000000000002','Разогрев арены','Сыграйте 25 дуэлей','DUELS','BRONZE',25,'DUEL_PLAYED'),
    ('d1000000-0000-4000-8000-000000000003','Стальной характер','Сыграйте 100 дуэлей','DUELS','SILVER',100,'DUEL_PLAYED'),
    ('d1000000-0000-4000-8000-000000000004','Легенда DuelRush','Сыграйте 500 дуэлей','DUELS','GOLD',500,'DUEL_PLAYED'),
    ('d2000000-0000-4000-8000-000000000001','Первое рукопожатие','Добавьте первого друга','FRIENDS','BRONZE',1,'FRIEND_ADDED'),
    ('d2000000-0000-4000-8000-000000000002','Учебная команда','Добавьте 10 друзей','FRIENDS','BRONZE',10,'FRIEND_ADDED'),
    ('d2000000-0000-4000-8000-000000000003','Языковой клуб','Добавьте 35 друзей','FRIENDS','SILVER',35,'FRIEND_ADDED'),
    ('d2000000-0000-4000-8000-000000000004','Центр сообщества','Добавьте 100 друзей','FRIENDS','GOLD',100,'FRIEND_ADDED'),
    ('d3000000-0000-4000-8000-000000000001','Первая десятка','Пройдите первый тест из 10 вопросов','TESTS','BRONZE',1,'TEST_PASSED'),
    ('d3000000-0000-4000-8000-000000000002','Точный ответ','Пройдите 10 тестов','TESTS','BRONZE',10,'TEST_PASSED'),
    ('d3000000-0000-4000-8000-000000000003','Грамматический навигатор','Пройдите 35 тестов','TESTS','SILVER',35,'TEST_PASSED'),
    ('d3000000-0000-4000-8000-000000000004','Архитектор языка','Пройдите 100 тестов','TESTS','GOLD',100,'TEST_PASSED'),
    ('d4000000-0000-4000-8000-000000000001','Первое слово','Добавьте первое слово в личный словарь','WORDS','BRONZE',1,'WORD_ADDED'),
    ('d4000000-0000-4000-8000-000000000002','Карманный словарь','Соберите 25 слов','WORDS','BRONZE',25,'WORD_ADDED'),
    ('d4000000-0000-4000-8000-000000000003','Коллекционер смыслов','Соберите 200 слов','WORDS','SILVER',200,'WORD_ADDED'),
    ('d4000000-0000-4000-8000-000000000004','Живая энциклопедия','Соберите 500 слов','WORDS','GOLD',500,'WORD_ADDED')
) AS v(id,title,description,type,level,required_value,condition_type)
WHERE NOT EXISTS (SELECT 1 FROM achievements a WHERE a.id = v.id::uuid);
