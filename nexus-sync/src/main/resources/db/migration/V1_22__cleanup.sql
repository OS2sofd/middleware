ALTER TABLE municipality DROP COLUMN do_not_use_sofd;

ALTER TABLE user DROP COLUMN last_updated;

ALTER TABLE user ADD COLUMN last_employee_update DATETIME NULL;
ALTER TABLE user ADD COLUMN last_organisation_update DATETIME NULL;
