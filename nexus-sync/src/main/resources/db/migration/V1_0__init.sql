CREATE TABLE municipality (
   id                                            BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
   cvr                                           VARCHAR(8) NOT NULL,
   name                                          VARCHAR(255) NOT NULL,
   role_catalogue_base_url                       VARCHAR(255) NOT NULL,
   role_catalogue_api_key                        VARCHAR(255) NOT NULL,
   role_catalogue_nexus_it_system_id             BIGINT NOT NULL,
   role_catalogue_nexus_it_system_identifier     VARCHAR(255) NOT NULL,
   role_catalogue_nexus_it_system_name           VARCHAR(255) NOT NULL,
   nexus_base_url                                VARCHAR(255) NOT NULL,
   nexus_token_url                               VARCHAR(255) NOT NULL,
   nexus_client_id                               VARCHAR(255) NOT NULL,
   nexus_root_org_unit_id                        BIGINT NOT NULL DEFAULT 1,
   nexus_client_secret                           VARCHAR(255) NOT NULL
);

CREATE TABLE user (
   id                                   BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
   user_id                              VARCHAR(255) NOT NULL,
   nexus_id                             BIGINT NOT NULL,
   last_updated                         TIMESTAMP NULL,
   deleted                              BOOLEAN NOT NULL DEFAULT FALSE,
   municipality_id                      BIGINT NOT NULL,

   CONSTRAINT fk_user_municipality FOREIGN KEY (municipality_id) REFERENCES municipality(id) ON DELETE CASCADE
);

CREATE TABLE assignment (
   id                                   BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
   nexus_org_unit_id                    BIGINT NOT NULL,
   rc_user_role_id                      BIGINT NOT NULL,
   user_id                              BIGINT NOT NULL,

   CONSTRAINT fk_assignment_use FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);