CREATE TABLE municipality (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name                            VARCHAR(255) NOT NULL,
  enabled                         BOOLEAN NOT NULL DEFAULT 0,

  bearer_token                    VARCHAR(255) NOT NULL,
  mail_domain                     VARCHAR(255) NULL,

  sofd_url                        VARCHAR(255) NOT NULL,
  sofd_api_key                    VARCHAR(36) NOT NULL
);
