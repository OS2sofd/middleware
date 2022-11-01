CREATE TABLE municipality (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name                            VARCHAR(255) NOT NULL,

  cics_keystore                   VARCHAR(255) NOT NULL,
  cics_password                   VARCHAR(255) NOT NULL,
  cics_los_id                     VARCHAR(255) NOT NULL,

  sofd_url                        VARCHAR(255) NOT NULL,
  sofd_api_key                    VARCHAR(36) NOT NULL,

  account_orders_enabled          BOOL NOT NULL DEFAULT 0
);
