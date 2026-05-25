-- =============================================================================
-- V1__init_schema.sql
-- Full database schema for the Project Management System
-- =============================================================================

-- -------------------------------------------------------------------------
-- ROLES — seeded with the five application roles
-- -------------------------------------------------------------------------
CREATE TABLE roles (
    id          SERIAL       PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE
);

-- -------------------------------------------------------------------------
-- USERS — core identity table, linked to Spring Security
-- -------------------------------------------------------------------------
CREATE TABLE users (
    id                      SERIAL          PRIMARY KEY,
    full_name               VARCHAR(100)    NOT NULL,
    email                   VARCHAR(150)    NOT NULL UNIQUE,
    password                TEXT            NOT NULL,
    phone                   VARCHAR(20),
    profile_image           VARCHAR(500),
    enabled                 BOOLEAN         NOT NULL DEFAULT FALSE,
    account_locked          BOOLEAN         NOT NULL DEFAULT FALSE,
    failed_login_attempts   INTEGER         NOT NULL DEFAULT 0,
    email_verified          BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP
);

-- -------------------------------------------------------------------------
-- USER_ROLES — many-to-many join between users and roles
-- -------------------------------------------------------------------------
CREATE TABLE user_roles (
    user_id     BIGINT  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id     BIGINT  NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- -------------------------------------------------------------------------
-- TEAMS
-- -------------------------------------------------------------------------
CREATE TABLE teams (
    id              SERIAL          PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(500),
    created_by      BIGINT          NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- -------------------------------------------------------------------------
-- TEAM_MEMBERS — explicit join entity with extra metadata
-- -------------------------------------------------------------------------
CREATE TABLE team_members (
    id          SERIAL      PRIMARY KEY,
    team_id     BIGINT      NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role        VARCHAR(50),
    joined_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP,
    UNIQUE (team_id, user_id)
);

-- -------------------------------------------------------------------------
-- PROJECTS
-- -------------------------------------------------------------------------
CREATE TABLE projects (
    id              SERIAL              PRIMARY KEY,
    title           VARCHAR(255)        NOT NULL,
    description     TEXT,
    status          VARCHAR(20)         NOT NULL DEFAULT 'NEW',
    priority        VARCHAR(20)         NOT NULL DEFAULT 'MEDIUM',
    start_date      DATE,
    end_date        DATE,
    progress        DOUBLE PRECISION    NOT NULL DEFAULT 0.0,
    created_by      BIGINT              NOT NULL REFERENCES users(id),
    team_id         BIGINT              REFERENCES teams(id) ON DELETE SET NULL,
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- -------------------------------------------------------------------------
-- MILESTONES
-- -------------------------------------------------------------------------
CREATE TABLE milestones (
    id              SERIAL          PRIMARY KEY,
    title           VARCHAR(255)    NOT NULL,
    description     VARCHAR(500),
    deadline        DATE            NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    project_id      BIGINT          NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- -------------------------------------------------------------------------
-- TASKS
-- -------------------------------------------------------------------------
CREATE TABLE tasks (
    id              SERIAL          PRIMARY KEY,
    title           VARCHAR(255)    NOT NULL,
    description     TEXT,
    status          VARCHAR(20)     NOT NULL DEFAULT 'TODO',
    priority        VARCHAR(20)     NOT NULL DEFAULT 'MEDIUM',
    deadline        DATE,
    project_id      BIGINT          NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    assigned_to     BIGINT          REFERENCES users(id) ON DELETE SET NULL,
    created_by      BIGINT          NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- -------------------------------------------------------------------------
-- COMMENTS
-- -------------------------------------------------------------------------
CREATE TABLE comments (
    id          SERIAL      PRIMARY KEY,
    message     TEXT        NOT NULL,
    task_id     BIGINT      NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id     BIGINT      NOT NULL REFERENCES users(id),
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP
);

-- -------------------------------------------------------------------------
-- MESSAGES — direct messaging between users
-- -------------------------------------------------------------------------
CREATE TABLE messages (
    id              SERIAL      PRIMARY KEY,
    content         TEXT        NOT NULL,
    sent_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_status     BOOLEAN     NOT NULL DEFAULT FALSE,
    sender_id       BIGINT      NOT NULL REFERENCES users(id),
    receiver_id     BIGINT      NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- -------------------------------------------------------------------------
-- NOTIFICATIONS
-- -------------------------------------------------------------------------
CREATE TABLE notifications (
    id          SERIAL          PRIMARY KEY,
    title       VARCHAR(255)    NOT NULL,
    message     TEXT            NOT NULL,
    type        VARCHAR(30)     NOT NULL,
    seen        BOOLEAN         NOT NULL DEFAULT FALSE,
    user_id     BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP
);

-- -------------------------------------------------------------------------
-- FILE_UPLOADS
-- -------------------------------------------------------------------------
CREATE TABLE file_uploads (
    id              SERIAL          PRIMARY KEY,
    file_name       VARCHAR(255)    NOT NULL,
    file_path       VARCHAR(500)    NOT NULL,
    file_type       VARCHAR(50),
    file_size       BIGINT,
    version         INTEGER         NOT NULL DEFAULT 1,
    project_id      BIGINT          REFERENCES projects(id) ON DELETE SET NULL,
    uploaded_by     BIGINT          NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- -------------------------------------------------------------------------
-- REPORTS
-- -------------------------------------------------------------------------
CREATE TABLE reports (
    id              SERIAL          PRIMARY KEY,
    report_type     VARCHAR(30)     NOT NULL,
    file_url        VARCHAR(500),
    parameters      TEXT,
    project_id      BIGINT          REFERENCES projects(id) ON DELETE SET NULL,
    generated_by    BIGINT          NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- -------------------------------------------------------------------------
-- AUDIT_LOGS — immutable trail of user actions
-- -------------------------------------------------------------------------
CREATE TABLE audit_logs (
    id              SERIAL          PRIMARY KEY,
    action          VARCHAR(50)     NOT NULL,
    entity_type     VARCHAR(50)     NOT NULL,
    entity_id       BIGINT,
    details         TEXT,
    ip_address      VARCHAR(45),
    user_id         BIGINT          REFERENCES users(id) ON DELETE SET NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- =========================================================================
-- INDEXES — optimise the most frequent query patterns
-- =========================================================================
CREATE INDEX idx_users_email             ON users(email);
CREATE INDEX idx_tasks_project           ON tasks(project_id);
CREATE INDEX idx_tasks_assigned          ON tasks(assigned_to);
CREATE INDEX idx_tasks_status            ON tasks(status);
CREATE INDEX idx_tasks_deadline          ON tasks(deadline);
CREATE INDEX idx_comments_task           ON comments(task_id);
CREATE INDEX idx_messages_sender         ON messages(sender_id);
CREATE INDEX idx_messages_receiver       ON messages(receiver_id);
CREATE INDEX idx_notifications_user      ON notifications(user_id);
CREATE INDEX idx_notifications_unseen    ON notifications(user_id, seen);
CREATE INDEX idx_file_uploads_project    ON file_uploads(project_id);
CREATE INDEX idx_projects_team           ON projects(team_id);
CREATE INDEX idx_projects_status         ON projects(status);
CREATE INDEX idx_projects_created_by     ON projects(created_by);
CREATE INDEX idx_team_members_team       ON team_members(team_id);
CREATE INDEX idx_team_members_user       ON team_members(user_id);
CREATE INDEX idx_audit_logs_user         ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity       ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_milestones_project      ON milestones(project_id);
CREATE INDEX idx_reports_project         ON reports(project_id);

-- =========================================================================
-- SEED DATA — default roles
-- =========================================================================
INSERT INTO roles (name) VALUES
    ('ADMIN'),
    ('PROJECT_MANAGER'),
    ('TEAM_LEADER'),
    ('TEAM_MEMBER'),
    ('PRODUCT_OWNER');
