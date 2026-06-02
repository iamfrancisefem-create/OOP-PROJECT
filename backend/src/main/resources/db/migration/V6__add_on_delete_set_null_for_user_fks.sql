-- Make FK columns nullable so user deletion can SET NULL on related records
ALTER TABLE projects      ALTER COLUMN created_by DROP NOT NULL;
ALTER TABLE teams         ALTER COLUMN created_by DROP NOT NULL;
ALTER TABLE comments      ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE file_uploads  ALTER COLUMN uploaded_by DROP NOT NULL;
ALTER TABLE reports       ALTER COLUMN generated_by DROP NOT NULL;
ALTER TABLE tasks         ALTER COLUMN created_by DROP NOT NULL;
