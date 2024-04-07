CREATE TABLE municipality (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name                            VARCHAR(255) NOT NULL,
  sofd_url                        VARCHAR(255) NOT NULL,
  sofd_api_key                    VARCHAR(36) NOT NULL,
  enabled                         BOOL NOT NULL DEFAULT TRUE,
  client_id                       VARCHAR(255) NOT NULL,
  client_secret                   VARCHAR(255) NOT NULL,
  tenant_id                       VARCHAR(255) NOT NULL,
  cpr_field                       VARCHAR(255) NOT NULL,
  user_id_field                   VARCHAR(255) NOT NULL
);