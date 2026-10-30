CREATE TABLE municipality (
  id                   BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name                 VARCHAR(255) NOT NULL,
  cvr                  VARCHAR(255) NULL,
  sofd_url             VARCHAR(255) NULL,
  sofd_apikey          VARCHAR(36) NULL,
  rolecatalog_url      VARCHAR(255) NULL,
  rolecatalog_apikey   VARCHAR(36) NULL,
  nexus_token_url      VARCHAR(255) NULL,
  nexus_apikey         VARCHAR(36) NULL,
  nexus_url            VARCHAR(255) NULL,
  os2faktor_url        VARCHAR(255) NULL,
  os2faktor_apikey     VARCHAR(36) NULL,
  ad_apikey            VARCHAR(36) NULL,
  
  allow_user_login     BOOLEAN NOT NULL DEFAULT 1,
  
  disabled             BOOLEAN NOT NULL DEFAULT 0
);

CREATE TABLE auditlog (
  id                   BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  tts                  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  cvr                  VARCHAR(8) NOT NULL,
  ip_address           VARCHAR(64) NOT NULL,
  admin                VARCHAR(255) NOT NULL,
  user_id              VARCHAR(255) NOT NULL,
  
  INDEX idx_auditlog_cvr (cvr),
  INDEX idx_auditlog_tts (tts),
  INDEX idx_auditlog_userid (user_id),
  INDEX idx_auditlog_admin (admin)
);
