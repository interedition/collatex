CREATE TABLE IF NOT EXISTS text_incrementer (
  text_qname_sequence BIGINT NOT NULL DEFAULT 0,
  text_content_sequence BIGINT NOT NULL DEFAULT 0,
  text_annotation_sequence BIGINT NOT NULL DEFAULT 0,
  text_annotation_link_sequence BIGINT NOT NULL DEFAULT 0
) ENGINE = MYISAM;

CREATE TABLE IF NOT EXISTS text_qname (
  id BIGINT PRIMARY KEY,
  local_name VARCHAR(100) NOT NULL,
  namespace VARCHAR(100),
  UNIQUE (local_name, namespace)
) ENGINE = INNODB;

CREATE TABLE IF NOT EXISTS text_qname_set (
  name BIGINT NOT NULL,
  member BIGINT NOT NULL,
  FOREIGN KEY (name) REFERENCES text_qname (id),
  FOREIGN KEY (member) REFERENCES text_qname (id),
  UNIQUE(name, member)
);

CREATE TABLE IF NOT EXISTS text_content (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  created TIMESTAMP NOT NULL,
  type SMALLINT NOT NULL,
  content LONGTEXT NOT NULL,
  content_length BIGINT NOT NULL
) ENGINE = INNODB;

CREATE TABLE IF NOT EXISTS text_annotation (
  id BIGINT PRIMARY KEY,
  text BIGINT NOT NULL,
  name BIGINT NOT NULL,
  range_start BIGINT NOT NULL,
  range_end BIGINT NOT NULL,
  FOREIGN KEY (text) REFERENCES text_content (id) ON DELETE CASCADE,
  FOREIGN KEY (name) REFERENCES text_qname (id),
  INDEX (range_start, range_end)
) ENGINE = INNODB;

CREATE TABLE IF NOT EXISTS text_annotation_link (
  id BIGINT PRIMARY KEY,
  name BIGINT NOT NULL,
  FOREIGN KEY (name) REFERENCES text_qname (id)
) ENGINE = INNODB;

CREATE TABLE IF NOT EXISTS text_annotation_link_target (
  link BIGINT NOT NULL,
  target BIGINT NOT NULL,
  FOREIGN KEY (link) REFERENCES text_annotation_link (id) ON DELETE CASCADE,
  FOREIGN KEY (target) REFERENCES text_annotation (id) ON DELETE CASCADE,
  UNIQUE (link, target)
) ENGINE = INNODB;

CREATE TABLE IF NOT EXISTS text_annotation_data (
  annotation BIGINT NOT NULL,
  name BIGINT NOT NULL,
  value VARCHAR(255) NOT NULL,
  FOREIGN KEY (annotation) REFERENCES text_annotation (id) ON DELETE CASCADE,
  FOREIGN KEY (name) REFERENCES text_qname (id),
  UNIQUE (annotation, name)
) ENGINE = INNODB;

CREATE TABLE IF NOT EXISTS text_annotation_link_data (
  link BIGINT NOT NULL,
  name BIGINT NOT NULL,
  value VARCHAR(255) NOT NULL,
  FOREIGN KEY (link) REFERENCES text_annotation_link (id) ON DELETE CASCADE,
  FOREIGN KEY (name) REFERENCES text_qname (id),
  UNIQUE (link, name)
) ENGINE = INNODB;

