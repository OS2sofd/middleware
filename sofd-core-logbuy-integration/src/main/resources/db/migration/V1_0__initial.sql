CREATE TABLE created_person (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  uuid                            VARCHAR(36) NOT NULL,
  first_name                      VARCHAR(255) NOT NULL,
  sur_name                        VARCHAR(255) NOT NULL,
  email                           VARCHAR(512) NOT NULL,
  salary_number                   VARCHAR(512) NOT NULL,
  gender                          VARCHAR(36) NOT NULL,
  status                          VARCHAR(36) NOT NULL,
  created                         datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  changed                         datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);
