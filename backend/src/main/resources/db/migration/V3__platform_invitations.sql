CREATE TABLE platform_invitations (
    id              SERIAL          PRIMARY KEY,
    email           VARCHAR(255)    NOT NULL,
    role            VARCHAR(50)     NOT NULL,
    token           VARCHAR(255)    NOT NULL UNIQUE,
    invited_by_id   BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    accepted        BOOLEAN         NOT NULL DEFAULT FALSE,
    expires_at      TIMESTAMP       NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

CREATE INDEX idx_platform_invitations_token ON platform_invitations(token);
CREATE INDEX idx_platform_invitations_email ON platform_invitations(email);
CREATE INDEX idx_platform_invitations_invited_by ON platform_invitations(invited_by_id);
