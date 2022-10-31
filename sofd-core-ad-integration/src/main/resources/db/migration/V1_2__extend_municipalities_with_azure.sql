ALTER TABLE municipality ADD COLUMN azure_lookup_enabled    BOOL NOT NULL DEFAULT false;
ALTER TABLE municipality ADD COLUMN azure_tenant_id         VARCHAR(255) NULL;
ALTER TABLE municipality ADD COLUMN azure_client_id         VARCHAR(255) NULL;
ALTER TABLE municipality ADD COLUMN azure_secret            VARCHAR(255) NULL;
ALTER TABLE municipality ADD COLUMN azure_domain            VARCHAR(255) NULL;