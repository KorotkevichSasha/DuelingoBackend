--liquibase formatted sql

--changeset duelrush:2026-08-06-add-duel-answer-history-v2
ALTER TABLE duel
    ADD COLUMN IF NOT EXISTS player1_answers TEXT,
    ADD COLUMN IF NOT EXISTS player2_answers TEXT;
