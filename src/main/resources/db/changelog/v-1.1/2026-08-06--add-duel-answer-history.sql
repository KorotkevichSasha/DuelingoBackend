--liquibase formatted sql

--changeset codex:2026-08-06-add-duel-answer-history
ALTER TABLE duel
    ADD COLUMN player1_answers TEXT,
    ADD COLUMN player2_answers TEXT;
