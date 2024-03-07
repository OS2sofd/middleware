ALTER TABLE municipality ADD COLUMN create_email_enabled BOOL NOT NULL DEFAULT 1;

UPDATE municipality SET create_email_enabled = 0 WHERE user_type != 'ACTIVE_DIRECTORY';