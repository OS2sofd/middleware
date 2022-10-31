CREATE TABLE municipality (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name                            VARCHAR(255) NOT NULL,

  bucket                          VARCHAR(255) NULL,

  api_key                         VARCHAR(36) NOT NULL,
  url                             VARCHAR(255) NOT NULL,

  medudvalg                       SMALLINT NOT NULL DEFAULT 0,
  sr                              SMALLINT NOT NULL DEFAULT 0,
  tr                              SMALLINT NOT NULL DEFAULT 0,
  tr_suppleant                    SMALLINT NOT NULL DEFAULT 0,
  
  manager_ou_for_level1           VARCHAR(255) NULL,
  manager_ou_for_level2           VARCHAR(255) NULL,
  manager_ou_for_level3           VARCHAR(255) NULL,
  manager_ou_for_level4           VARCHAR(255) NULL,
  
  filter_efter_indtaegt           BOOL NOT NULL DEFAULT 0,
  include_wage_step               BOOL NOT NULL DEFAULT 0,
  
  use_s3_file_share               BOOL NOT NULL DEFAULT 0,
  
  s3_file_share_api_key           VARCHAR(36) NULL
);
