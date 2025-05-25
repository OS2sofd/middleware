ALTER TABLE municipality ADD initials_choice varchar(255) DEFAULT 'USERID' NOT NULL;

UPDATE municipality SET initials_choice = 'USERID' WHERE generate_initials = false;
UPDATE municipality SET initials_choice = 'GENERATE' WHERE generate_initials = true;

ALTER TABLE municipality DROP COLUMN generate_initials;
