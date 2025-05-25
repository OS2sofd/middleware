ALTER TABLE municipality ADD COLUMN role_catalogue_kmd_nexus_it_system_id BIGINT NULL AFTER role_catalogue_nexus_it_system_name;
ALTER TABLE municipality ADD COLUMN role_catalogue_trust_user_role_id BIGINT NULL AFTER role_catalogue_kmd_nexus_it_system_id;
ALTER TABLE municipality ADD COLUMN nexus_default_department VARCHAR(255) NULL;
ALTER TABLE municipality ADD COLUMN nexus_dummy_email_address VARCHAR(255) NULL;
ALTER TABLE municipality ADD COLUMN nexus_default_postal_code VARCHAR(255) NULL;
ALTER TABLE municipality ADD COLUMN nexus_default_address_line VARCHAR(255) NULL;
ALTER TABLE municipality ADD COLUMN nexus_default_professional_job_name VARCHAR(255) NULL;
ALTER TABLE municipality ADD COLUMN sofd_base_url VARCHAR(255) NULL;
ALTER TABLE municipality ADD COLUMN sofd_api_key VARCHAR(255) NULL;
ALTER TABLE municipality ADD COLUMN sofd_sync_head BIGINT NOT NULL DEFAULT 0;
ALTER TABLE municipality ADD COLUMN fk_org_base_url VARCHAR(255) NULL;

ALTER TABLE user ADD COLUMN always_in_ous VARCHAR(500) NULL;

CREATE TABLE created_nexus_user (
   id                                            BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
   cvr                                           VARCHAR(8) NOT NULL,
   user_id                                       VARCHAR(64) NOT NULL,
   sofd_person_uuid                              VARCHAR(36) NOT NULL,
   associated_user_ids                           VARCHAR(255) NULL
);

CREATE TABLE nexus_sofd_position_mapping (
   id                                            BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
   cvr                                           VARCHAR(8) NOT NULL,
   sofd_position                                 VARCHAR(255) NOT NULL,
   nexus_professional_job                        VARCHAR(255) NOT NULL,
   nexus_fmk_role                                VARCHAR(255) NULL
);

CREATE TABLE update_configuration (
   id                                                             BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
   cvr                                                            VARCHAR(8) NOT NULL,
   organisation                                                   BOOLEAN NOT NULL DEFAULT FALSE,
   unit_title                                                     BOOLEAN NOT NULL DEFAULT FALSE,
   professional_job                                               BOOLEAN NOT NULL DEFAULT FALSE,
   primary_org_and_default_org_supplier_and_medcom_sender_org     BOOLEAN NOT NULL DEFAULT FALSE,
   authorisation_code                                             BOOLEAN NOT NULL DEFAULT FALSE,
   fmk_role                                                       BOOLEAN NOT NULL DEFAULT FALSE,
   upn                                                            BOOLEAN NOT NULL DEFAULT FALSE
);
