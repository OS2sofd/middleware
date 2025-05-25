ALTER TABLE municipality ADD COLUMN send_to_exchange_default_value BOOLEAN NOT NULL DEFAULT 0;
ALTER TABLE municipality ADD COLUMN reply_to_default_medcom_sender_organization_default_value BOOLEAN NOT NULL DEFAULT 1;
ALTER TABLE municipality ADD COLUMN national_role_default_value VARCHAR(128) NULL;
