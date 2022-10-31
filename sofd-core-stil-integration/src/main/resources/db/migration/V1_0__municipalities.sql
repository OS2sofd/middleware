CREATE TABLE municipality (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name                            VARCHAR(255) NOT NULL,

  stil_username                   VARCHAR(255) NOT NULL,
  stil_password                   VARCHAR(255) NOT NULL,

  sofd_url                        VARCHAR(255) NOT NULL,
  sofd_api_key                    VARCHAR(36) NOT NULL,

  enable_email                    BOOL NOT NULL DEFAULT TRUE,
  email_suffix                    VARCHAR(255) NULL
);

CREATE TABLE municipality_institution (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  municipality_id                 BIGINT NOT NULL,
  institution                     VARCHAR(255) NOT NULL,
  FOREIGN KEY (municipality_id) REFERENCES municipality(id)
)