-- =============================================================================
-- V2__project_invitations.sql
-- Schema addition for team invitation and project sharing
-- =============================================================================

CREATE TABLE project_invitations (
    id              SERIAL          PRIMARY KEY,
    project_id      BIGINT          NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    invited_user_id BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token           VARCHAR(255)    NOT NULL UNIQUE,
    expires_at      TIMESTAMP       NOT NULL,
    accepted        BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

CREATE INDEX idx_project_invitations_token ON project_invitations(token);
CREATE INDEX idx_project_invitations_user ON project_invitations(invited_user_id);
