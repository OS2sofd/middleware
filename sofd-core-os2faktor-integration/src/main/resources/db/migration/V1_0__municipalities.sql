CREATE TABLE municipality (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name                            VARCHAR(255) NOT NULL,
  allow_nsis_for_everyone         BOOL NOT NULL DEFAULT TRUE,

  os2faktor_domain                VARCHAR(255) NOT NULL,
  os2faktor_url                   VARCHAR(255) NOT NULL,
  os2faktor_api_key               VARCHAR(255) NOT NULL,

  sofd_url                        VARCHAR(255) NOT NULL,
  sofd_api_key                    VARCHAR(36) NOT NULL,

  role_catalog_enabled            BOOL NOT NULL DEFAULT FALSE,

  role_catalog_url                VARCHAR(255) NULL,
  role_catalog_api_key            VARCHAR(255) NULL,
  role_catalog_role_id            VARCHAR(255) NULL
);