CREATE TABLE achievements (
      id UUID PRIMARY KEY,
      title VARCHAR(255) NOT NULL,
      description TEXT NOT NULL,
      type VARCHAR(20) NOT NULL,
      level VARCHAR(20) NOT NULL,
      required_value INT NOT NULL,
      condition_type VARCHAR(50) NOT NULL,
      icon_url VARCHAR(255),
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_achievements_type_level ON achievements(type, level);
CREATE INDEX IF NOT EXISTS idx_achievements_condition_type ON achievements(condition_type);

CREATE TABLE user_achievements (
       id UUID PRIMARY KEY,
       user_id UUID NOT NULL,
       achievement_id UUID NOT NULL,
       current_value INT DEFAULT 0 NOT NULL,
       is_achieved BOOLEAN DEFAULT FALSE NOT NULL,
       achieved_at TIMESTAMP,
       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
       FOREIGN KEY (achievement_id) REFERENCES achievements(id) ON DELETE CASCADE,
       CONSTRAINT uq_user_achievement UNIQUE(user_id, achievement_id)
);

CREATE INDEX IF NOT EXISTS idx_user_achievements_user_id ON user_achievements(user_id);
CREATE INDEX IF NOT EXISTS idx_user_achievements_achievement_id ON user_achievements(achievement_id);

-- Пример добавления ачивок с иконками
INSERT INTO achievements (id, title, description, type, level, required_value, condition_type, icon_url)
VALUES
    (gen_random_uuid(), 'Новичок дуэлей', 'Сыграй 10 дуэлей', 'DUELS', 'BRONZE', 10, 'DUEL_PLAYED', 'https://img.icons8.com/fluency/48/sword.png'),
    (gen_random_uuid(), 'Опытный дуэлянт', 'Сыграй 50 дуэлей', 'DUELS', 'SILVER', 50, 'DUEL_PLAYED', 'https://img.icons8.com/fluency/48/sword.png'),
    (gen_random_uuid(), 'Мастер дуэлей', 'Сыграй 200 дуэлей', 'DUELS', 'GOLD', 200, 'DUEL_PLAYED', 'https://img.icons8.com/fluency/48/sword.png'),

    -- FRIENDS
    (gen_random_uuid(), 'Общительный', 'Добавь 5 друзей', 'FRIENDS', 'BRONZE', 5, 'FRIEND_ADDED', 'https://img.icons8.com/color/48/add-user-group-man-man.png'),
    (gen_random_uuid(), 'Популярен', 'Добавь 20 друзей', 'FRIENDS', 'SILVER', 20, 'FRIEND_ADDED', 'https://img.icons8.com/color/48/add-user-group-man-man.png'),
    (gen_random_uuid(), 'Звезда сети', 'Добавь 50 друзей', 'FRIENDS', 'GOLD', 50, 'FRIEND_ADDED', 'https://img.icons8.com/color/48/add-user-group-man-man.png'),

    -- TESTS
    (gen_random_uuid(), 'Ученик', 'Пройди 5 тестов', 'TESTS', 'BRONZE', 5, 'TEST_PASSED', 'https://img.icons8.com/color/48/student-center.png'),
    (gen_random_uuid(), 'Студент', 'Пройди 20 тестов', 'TESTS', 'SILVER', 20, 'TEST_PASSED', 'https://img.icons8.com/color/48/student-center.png'),
    (gen_random_uuid(), 'Профи', 'Пройди 50 тестов', 'TESTS', 'GOLD', 50, 'TEST_PASSED', 'https://img.icons8.com/color/48/student-center.png'),

    -- WORDS
    (gen_random_uuid(), 'Словарик', 'Добавь 10 слов', 'WORDS', 'BRONZE', 10, 'WORD_ADDED', 'https://img.icons8.com/color/48/book.png'),
    (gen_random_uuid(), 'Толковый', 'Добавь 100 слов', 'WORDS', 'SILVER', 100, 'WORD_ADDED', 'https://img.icons8.com/color/48/book.png'),
    (gen_random_uuid(), 'Филолог', 'Добавь 300 слов', 'WORDS', 'GOLD', 300, 'WORD_ADDED', 'https://img.icons8.com/color/48/book.png');


