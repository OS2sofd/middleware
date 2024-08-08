CREATE TABLE municipality (
  id                                BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name                              VARCHAR(64) NOT NULL,
  sofd_url                          VARCHAR(128) NOT NULL,
  sofd_api_key                      VARCHAR(36) NOT NULL,
  kommunekode                       VARCHAR(10) NOT NULL,
  tabulex_api_key                   VARCHAR(64) NOT NULL,
  dry_run                           BOOLEAN NOT NULL DEFAULT 1,
  days_after_affiliation_stops      BIGINT NOT NULL DEFAULT 3,
  days_before_affiliation_starts    BIGINT NOT NULL DEFAULT 3,
  tag_name                          VARCHAR(64) NOT NULL DEFAULT 'Institutionsnummer' 
);