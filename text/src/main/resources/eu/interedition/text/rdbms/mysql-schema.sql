CREATE TABLE IF NOT EXISTS text_qname (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  local_name VARCHAR(100) NOT NULL,
  namespace VARCHAR(100),
  UNIQUE (local_name, namespace)
) ENGINE = INNODB;

CREATE TABLE IF NOT EXISTS text_content (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  created TIMESTAMP NOT NULL,
  type SMALLINT NOT NULL,
  content LONGTEXT NOT NULL
) ENGINE = INNODB;

CREATE TABLE IF NOT EXISTS text_annotation (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  text BIGINT NOT NULL,
  name BIGINT NOT NULL,
  range_start BIGINT NOT NULL,
  range_end BIGINT NOT NULL,
  FOREIGN KEY (text) REFERENCES text_content (id) ON DELETE CASCADE,
  FOREIGN KEY (name) REFERENCES text_qname (id),
  INDEX (range_start, range_end)
) ENGINE = INNODB;

CREATE TABLE IF NOT EXISTS text_annotation_data (
  annotation BIGINT NOT NULL,
  name BIGINT NOT NULL,
  value VARCHAR(255) NOT NULL,
  FOREIGN KEY (annotation) REFERENCES text_annotation (id) ON DELETE CASCADE,
  FOREIGN KEY (name) REFERENCES text_qname (id),
  UNIQUE (annotation, name)
) ENGINE = INNODB;

CREATE TABLE IF NOT EXISTS text_annotation_set (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name BIGINT NOT NULL,
  FOREIGN KEY (name) REFERENCES text_qname (id)
) ENGINE = INNODB;

CREATE TABLE IF NOT EXISTS text_annotation_set_member (
  annotation_set BIGINT NOT NULL,
  annotation BIGINT NOT NULL,
  FOREIGN KEY (annotation_set) REFERENCES text_annotation_set (id) ON DELETE CASCADE,
  FOREIGN KEY (annotation) REFERENCES text_annotation (id) ON DELETE CASCADE,
  UNIQUE (annotation_set, annotation)
) ENGINE = INNODB;