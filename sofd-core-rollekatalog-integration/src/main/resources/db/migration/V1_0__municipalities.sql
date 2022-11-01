CREATE TABLE municipality (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name                            VARCHAR(255) NOT NULL,

  sofd_url                        VARCHAR(255) NOT NULL,
  sofd_api_key                    VARCHAR(36) NOT NULL,

  role_catalog_url                VARCHAR(255) NOT NULL,
  role_catalog_api_key            VARCHAR(255) NOT NULL,

  titles_enabled                  BOOL NOT NULL DEFAULT FALSE,
  delta_sync_enabled              BOOL NOT NULL DEFAULT FALSE
);