CREATE TABLE group_map (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  pattern                         VARCHAR(255) NOT NULL,
  municipality_id                 BIGINT NOT NULL,

  FOREIGN KEY (municipality_id) REFERENCES municipality(id) ON DELETE CASCADE
);