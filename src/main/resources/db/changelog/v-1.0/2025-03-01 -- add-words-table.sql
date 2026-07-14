CREATE TABLE user_words (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    term VARCHAR(100) NOT NULL,
    translation VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE (user_id, term)
);

CREATE TABLE user_word_progress (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    word_id UUID NOT NULL REFERENCES user_words(id) ON DELETE CASCADE,
    repetitions INTEGER NOT NULL DEFAULT 0,
    easiness_factor DOUBLE PRECISION NOT NULL DEFAULT 2.5,
    interval_days INTEGER NOT NULL DEFAULT 1,
    next_review_date DATE NOT NULL DEFAULT CURRENT_DATE,
    last_reviewed TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (user_id, word_id)
);

CREATE INDEX idx_words_user ON user_words(user_id);
CREATE INDEX idx_words_created_at ON user_words(created_at);
CREATE INDEX idx_progress_review ON user_word_progress(next_review_date);
CREATE INDEX idx_progress_user ON user_word_progress(user_id);

CREATE INDEX idx_words_search ON user_words
    USING GIN(to_tsvector('english', term || ' ' || translation));