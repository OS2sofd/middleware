-- default true for new installations
ALTER TABLE municipality ADD COLUMN ad_v3 BOOLEAN NOT NULL DEFAULT 1;

-- all exising starts at false
UPDATE municipality SET ad_v3 = 0;
