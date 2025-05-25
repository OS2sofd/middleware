DROP TABLE created_nexus_user;
DROP TABLE assignment;
DROP TABLE user;

CREATE TABLE user (
   id                                   BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
   user_id                              VARCHAR(255) NOT NULL,
   nexus_id                             BIGINT NOT NULL,
   sofd_person_uuid                     VARCHAR(36) NOT NULL,
   inactive_in_nexus                    BOOLEAN NOT NULL DEFAULT FALSE,
   failed_to_create                     BOOLEAN NOT NULL DEFAULT FALSE,
   failure_reason                       VARCHAR(64) NOT NULL DEFAULT 'NONE',
   ous_from_affiliations                VARCHAR(2048) NULL,
   created                              DATETIME NULL,
   last_updated                         DATETIME NULL,
   municipality_id                      BIGINT NOT NULL,

   CONSTRAINT fk_user_municipality FOREIGN KEY (municipality_id) REFERENCES municipality(id) ON DELETE CASCADE
);

CREATE TABLE assignment (
   id                                   BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
   org_unit_id                          BIGINT NOT NULL,
   user_id                              BIGINT NOT NULL,

   CONSTRAINT fk_assignment_use FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);