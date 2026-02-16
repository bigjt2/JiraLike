-- ============================================================
-- V1__create_tables.sql
-- Creates all application tables.
-- Uses IF NOT EXISTS on every object so re-running is safe.
-- ============================================================

-- ------------------------------------------------------------
-- app_users
-- Maps to: com.jiralike.entity.AppUser
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app_users (
    id          BIGSERIAL    PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    email       VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    avatar_url  TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_app_users_username UNIQUE (username),
    CONSTRAINT uq_app_users_email    UNIQUE (email)
);

-- ------------------------------------------------------------
-- projects
-- Maps to: com.jiralike.entity.Project
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS projects (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    key         VARCHAR(10)  NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_projects_key UNIQUE (key)
);

-- ------------------------------------------------------------
-- board_columns
-- Maps to: com.jiralike.entity.BoardColumn
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS board_columns (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    position   INTEGER      NOT NULL,
    color      VARCHAR(20),
    project_id BIGINT       NOT NULL,

    CONSTRAINT fk_board_columns_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_board_columns_project_id
    ON board_columns (project_id);

-- ------------------------------------------------------------
-- tickets
-- Maps to: com.jiralike.entity.Ticket
-- Enums stored as VARCHAR: Priority (LOW/MEDIUM/HIGH/CRITICAL)
--                          TicketType (STORY/BUG/TASK/EPIC/SUBTASK)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tickets (
    id           BIGSERIAL    PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    priority     VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    ticket_type  VARCHAR(20)  NOT NULL DEFAULT 'TASK',
    position     INTEGER      NOT NULL DEFAULT 0,
    story_points INTEGER,
    due_date     DATE,
    project_id   BIGINT       NOT NULL,
    column_id    BIGINT       NOT NULL,
    assignee_id  BIGINT,
    reporter_id  BIGINT,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_tickets_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_tickets_column
        FOREIGN KEY (column_id)  REFERENCES board_columns (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_tickets_assignee
        FOREIGN KEY (assignee_id) REFERENCES app_users (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_tickets_reporter
        FOREIGN KEY (reporter_id) REFERENCES app_users (id)
        ON DELETE SET NULL,
    CONSTRAINT chk_tickets_priority
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_tickets_ticket_type
        CHECK (ticket_type IN ('STORY', 'BUG', 'TASK', 'EPIC', 'SUBTASK'))
);

CREATE INDEX IF NOT EXISTS idx_tickets_project_id  ON tickets (project_id);
CREATE INDEX IF NOT EXISTS idx_tickets_column_id   ON tickets (column_id);
CREATE INDEX IF NOT EXISTS idx_tickets_assignee_id ON tickets (assignee_id);
CREATE INDEX IF NOT EXISTS idx_tickets_reporter_id ON tickets (reporter_id);

-- ------------------------------------------------------------
-- comments
-- Maps to: com.jiralike.entity.Comment
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comments (
    id         BIGSERIAL   PRIMARY KEY,
    content    TEXT        NOT NULL,
    ticket_id  BIGINT      NOT NULL,
    author_id  BIGINT      NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_comments_ticket
        FOREIGN KEY (ticket_id) REFERENCES tickets (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_comments_author
        FOREIGN KEY (author_id) REFERENCES app_users (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_comments_ticket_id ON comments (ticket_id);
