ALTER TABLE created_nexus_user ADD COLUMN primary_user VARCHAR(64) NOT NULL DEFAULT '';
UPDATE created_nexus_user SET primary_user = user_id;
