--liquibase formatted sql
--changeset duelrush:2026-08-07-add-user-reports
CREATE TABLE user_report (
    id UUID PRIMARY KEY,
    reporter_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reported_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reason VARCHAR(80) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_user_report_reported_created ON user_report (reported_user_id, created_at DESC);
CREATE INDEX idx_user_report_reporter_created ON user_report (reporter_id, created_at DESC);
