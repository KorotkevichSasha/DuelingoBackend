-- liquibase formatted sql

-- changeset leha:1737232154264-1
CREATE TYPE user_role_type AS ENUM ('ADMIN', 'USER');

-- changeset leha:1737232154264-2
CREATE TYPE user_progress_status_type AS ENUM ('IN_PROGRESS', 'COMPLETED');

-- changeset leha:1737232154264-3
CREATE TYPE user_relationship_status_type AS ENUM ('FRIEND_REQUEST', 'FRIEND', 'BLOCK');

-- changeset leha:1738421841059-1
CREATE TABLE users
(
    id            UUID          NOT NULL,
    username      VARCHAR(50)   NOT NULL,
    password      VARCHAR(255)  NOT NULL,
    email         VARCHAR(255)  NOT NULL,
    role          user_role_type NOT NULL,
    points        INTEGER       NOT NULL DEFAULT 0,
    avatar_url    VARCHAR(255),
    last_login    TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uc_users_email UNIQUE (email),
    CONSTRAINT uc_users_username UNIQUE (username)
);

-- changeset leha:1738421841059-2
CREATE TABLE user_test_progress
(
    id            UUID                        NOT NULL,
    user_id       UUID                        NOT NULL,
    test_id       VARCHAR(255)                NOT NULL,
    status        user_progress_status_type   NOT NULL,
    started_at    TIMESTAMP WITHOUT TIME ZONE,
    completed_at  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_user_test_progress PRIMARY KEY (user_id, test_id),
    CONSTRAINT fk_user_test_progress_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- changeset leha:1738421841059-3
CREATE TABLE user_relationship
(
    id            UUID                        NOT NULL,
    from_user_id         UUID                   NOT NULL,
    to_user_id      UUID                        NOT NULL,
    status          user_relationship_status_type NOT NULL,
    CONSTRAINT pk_user_relationship PRIMARY KEY (id),
    CONSTRAINT uc_relationship_pair UNIQUE (from_user_id, to_user_id),
    CONSTRAINT fk_user_relationship_user FOREIGN KEY (from_user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_relationship_related_user FOREIGN KEY (to_user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- changeset leha:1738421841059-4
CREATE TABLE duel
(
    id            UUID NOT NULL,
    player1_id    UUID,
    player2_id    UUID,
    player1_score INTEGER,
    player2_score INTEGER,
    started_at    TIMESTAMP WITHOUT TIME ZONE,
    ended_at      TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_duel PRIMARY KEY (id)
);

-- changeset leha:1738421841059-5
ALTER TABLE duel
    ADD CONSTRAINT FK_DUEL_ON_PLAYER1 FOREIGN KEY (player1_id) REFERENCES users (id) ON DELETE CASCADE;

-- changeset leha:1738421841059-6
ALTER TABLE duel
    ADD CONSTRAINT FK_DUEL_ON_PLAYER2 FOREIGN KEY (player2_id) REFERENCES users (id) ON DELETE CASCADE;

-- changeset leha:1738421841059-7
ALTER TABLE user_relationship
    ADD CONSTRAINT FK_USER_RELATIONSHIP_ON_RELATED_USER FOREIGN KEY (to_user_id) REFERENCES users (id) ON DELETE CASCADE;

-- changeset leha:1738421841059-8
ALTER TABLE user_relationship
    ADD CONSTRAINT FK_USER_RELATIONSHIP_ON_USER FOREIGN KEY (from_user_id) REFERENCES users (id) ON DELETE CASCADE;
