ALTER TABLE municipality ADD COLUMN full_sync_lower_bound BIGINT NOT NULL DEFAULT 500;
ALTER TABLE municipality ADD COLUMN full_sync_upper_bound BIGINT NOT NULL DEFAULT 20000;
