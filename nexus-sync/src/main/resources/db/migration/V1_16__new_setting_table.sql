CREATE TABLE municipality_settings (
   id                                                             BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
   cvr                                                            VARCHAR(8) NOT NULL,
   update_upn                                                     VARCHAR(64) NULL,
   missing_vendors_mail                                           VARCHAR(1000) NULL,
   create_failed_email                                            VARCHAR(255) NULL,
   disable_initials_update                                        BOOLEAN NOT NULL DEFAULT FALSE,
   organisation_name_update_type                                  VARCHAR(64) NULL,
   nexus_default_department                                       VARCHAR(255) NULL,
   nexus_unit_update_type                                         VARCHAR(64) NULL,
   nexus_unit_fetch_from                                          VARCHAR(255) NULL,
   nexus_dummy_email_address                                      VARCHAR(255) NULL,
   mobile_update_type                                             VARCHAR(64) NULL,
   work_phone_update_type                                         VARCHAR(64) NULL,
   address_update_type                                            VARCHAR(64) NULL,
   address_line_fetch_from                                        VARCHAR(64) NULL,
   address_line_default                                           VARCHAR(255) NULL,
   postal_code_fetch_from                                         VARCHAR(64) NULL,
   postal_code_default                                            VARCHAR(255) NULL,
   city_fetch_from                                                VARCHAR(64) NULL,
   city_default                                                   VARCHAR(255) NULL,
   professional_job_update_type                                   VARCHAR(64) NULL,
   professional_job_fetch_from                                    VARCHAR(64) NULL,
   professional_job_default                                       VARCHAR(64) NULL,
   orgs_update_type                                               VARCHAR(64) NULL,
   authorisation_code_update_type                                 VARCHAR(64) NULL,
   send_to_exchange_type                                          VARCHAR(64) NULL,
   use_default_medcom_sender_type                                 VARCHAR(64) NULL,
   trust_type                                                     VARCHAR(64) NULL,
   national_role_default_value                                    VARCHAR(64) NULL,
   fmk_role_update_type                                           VARCHAR(64) NULL,
   fmk_role_fetch_from                                            VARCHAR(64) NULL
);

INSERT INTO municipality_settings (cvr, update_upn, missing_vendors_mail, create_failed_email, disable_initials_update,
organisation_name_update_type, nexus_default_department, nexus_unit_update_type, nexus_unit_fetch_from, nexus_dummy_email_address,
mobile_update_type, work_phone_update_type, address_update_type, address_line_fetch_from, address_line_default, postal_code_fetch_from,
postal_code_default, city_fetch_from, professional_job_update_type, professional_job_fetch_from, professional_job_default,
orgs_update_type, authorisation_code_update_type, send_to_exchange_type, use_default_medcom_sender_type, trust_type,
national_role_default_value, fmk_role_update_type, fmk_role_fetch_from)
SELECT
   m.cvr,
   CASE
      WHEN u.upn = 0 THEN 'ONLY_CREATE'
      ELSE 'UPDATE'
   END,
   m.email,
   m.create_failed_email,
   m.disable_initials_update,
   CASE
      WHEN u.organisation = 0 THEN 'ONLY_CREATE'
      ELSE 'UPDATE'
   END,
   m.nexus_default_department,
   CASE
      WHEN u.unit_title = 0 THEN 'ONLY_CREATE'
      ELSE 'UPDATE'
   END,
   'FROM_SOFD',
   m.nexus_dummy_email_address,
   'ONLY_CREATE',
   'NO',
   'ONLY_CREATE',
   'DEFAULT_DATA',
   m.nexus_default_address_line,
   'DEFAULT_DATA',
   m.nexus_default_postal_code,
   'DEFAULT_DATA',
   CASE
      WHEN u.professional_job = 0 THEN 'ONLY_CREATE'
      ELSE 'UPDATE'
   END,
   'FROM_SOFD',
   m.nexus_default_professional_job_name,
   CASE
      WHEN u.primary_org_and_default_org_supplier_and_medcom_sender_org = 0 THEN 'ONLY_CREATE'
      ELSE 'UPDATE'
   END,
   CASE
      WHEN u.authorisation_code = 0 THEN 'ONLY_CREATE'
      ELSE 'UPDATE'
   END,
   CASE
      WHEN m.send_to_exchange_default_value = 0 THEN 'FALSE'
      ELSE 'TRUE'
   END,
   CASE
      WHEN m.reply_to_default_medcom_sender_organization_default_value = 0 THEN 'FALSE'
      ELSE 'TRUE'
   END,
   CASE
      WHEN m.role_catalogue_trust_user_role_id = 0  OR m.role_catalogue_trust_user_role_id IS NULL THEN 'NONE'
      ELSE 'ROLE_CATALOG'
   END,
   CASE
      WHEN m.national_role_default_value IS NULL THEN 'NONE'
      ELSE m.national_role_default_value
   END,
   CASE
      WHEN u.fmk_role = 0 THEN 'ONLY_CREATE'
      ELSE 'UPDATE'
   END,
   'FROM_SOFD'
FROM municipality m
JOIN update_configuration u on u.cvr = m.cvr;

ALTER TABLE municipality
   DROP COLUMN nexus_default_department,
   DROP COLUMN role_catalogue_nexus_it_system_identifier,
   DROP COLUMN role_catalogue_nexus_it_system_name,
   DROP COLUMN nexus_root_org_unit_id,
   DROP COLUMN nexus_dummy_email_address,
   DROP COLUMN nexus_default_postal_code,
   DROP COLUMN nexus_default_address_line,
   DROP COLUMN nexus_default_professional_job_name,
   DROP COLUMN email,
   DROP COLUMN send_to_exchange_default_value,
   DROP COLUMN reply_to_default_medcom_sender_organization_default_value,
   DROP COLUMN national_role_default_value,
   DROP COLUMN create_failed_email,
   DROP COLUMN disable_initials_update,
   DROP COLUMN role_catalogue_trust_user_role_id,
   ADD COLUMN role_catalogue_trust_role_id VARCHAR(64) NULL,
   ADD COLUMN role_catalogue_nexus_flag_it_system_identifier VARCHAR(64) NULL,
   ADD COLUMN role_catalogue_nexus_fmk_role_it_system_identifier VARCHAR(64) NULL,
   ADD COLUMN role_catalogue_nexus_national_role_it_system_identifier VARCHAR(64) NULL,
   ADD COLUMN initial_trust_sync_done BOOLEAN NOT NULL DEFAULT FALSE;

DROP TABLE update_configuration;

UPDATE municipality SET disabled = 1;

ALTER TABLE user DROP COLUMN inactive_in_nexus;