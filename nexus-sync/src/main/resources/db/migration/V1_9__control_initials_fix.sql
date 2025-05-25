ALTER TABLE user DROP COLUMN generate_initials;
ALTER TABLE municipality ADD COLUMN generate_initials BOOLEAN NOT NULL DEFAULT FALSE;