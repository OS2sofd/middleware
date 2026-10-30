ALTER TABLE municipality ADD COLUMN nexus_enabled BOOLEAN NOT NULL DEFAULT 0;
ALTER TABLE municipality DROP COLUMN nexus_token_url;
ALTER TABLE municipality DROP COLUMN nexus_apikey;
ALTER TABLE municipality DROP COLUMN nexus_url;
