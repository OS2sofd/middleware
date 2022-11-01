CREATE TABLE municipality (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name                            VARCHAR(255) NOT NULL,
  password                        VARCHAR(255) NOT NULL,

  sofd_url                        VARCHAR(255) NOT NULL,
  sofd_api_key                    VARCHAR(36) NOT NULL
);