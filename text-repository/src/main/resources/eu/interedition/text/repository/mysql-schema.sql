CREATE TABLE IF NOT EXISTS repository_text_collection (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  UNIQUE (name)
) ENGINE = INNODB;

CREATE TABLE IF NOT EXISTS repository_text_metadata (
  text BIGINT NOT NULL,
  created TIMESTAMP NOT NULL,
  updated TIMESTAMP NOT NULL,
  collection BIGINT,
  title VARCHAR(255),
  summary VARCHAR(255),
  author VARCHAR(255),
  FOREIGN KEY (text) REFERENCES text_content (id) ON DELETE CASCADE,
  FOREIGN KEY (collection) REFERENCES repository_text_collection (id) ON DELETE CASCADE,
  UNIQUE (text, collection)
) ENGINE = INNODB;
