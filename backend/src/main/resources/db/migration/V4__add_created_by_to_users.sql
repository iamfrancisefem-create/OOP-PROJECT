ALTER TABLE users ADD COLUMN created_by_id BIGINT REFERENCES users(id) ON DELETE SET NULL;
UPDATE users SET created_by_id = id;
