-- liquibase formatted sql

-- changeset leha:20231010-01-add-pg_trgm-extension
-- precondition: check if pg_trgm extension is not installed
-- comment: Упрощенный запрос для создания расширения
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- changeset leha:20231010-02-add-trigram-index
-- precondition: check if index idx_users_username_trgm does not exist
-- comment: Создание триграммного индекса
CREATE INDEX IF NOT EXISTS idx_users_username_trgm
    ON users USING gin (username gin_trgm_ops);