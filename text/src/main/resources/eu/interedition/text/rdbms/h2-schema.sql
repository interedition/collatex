CREATE TABLE text_qname (
  id BIGINT IDENTITY,
  local_name VARCHAR(255) NOT NULL,
  namespace VARCHAR(255),
  UNIQUE (local_name, namespace)
);

CREATE TABLE text_content (
  id BIGINT IDENTITY,
  created TIMESTAMP NOT NULL,
  type SMALLINT NOT NULL,
  content CLOB NOT NULL
);

CREATE TABLE text_annotation (
  id BIGINT IDENTITY,
  text BIGINT NOT NULL REFERENCES text_content (id) ON DELETE CASCADE,
  name BIGINT NOT NULL REFERENCES text_qname (id),
  range_start BIGINT NOT NULL,
  range_end BIGINT NOT NULL
);

CREATE INDEX text_annotation_ranges ON text_annotation (range_start, range_end);

CREATE TABLE text_annotation_link (
  id BIGINT IDENTITY,
  name BIGINT NOT NULL REFERENCES text_qname (id)
);

CREATE TABLE text_annotation_link_target (
  link BIGINT NOT NULL REFERENCES text_annotation_link (id) ON DELETE CASCADE,
  target BIGINT NOT NULL REFERENCES text_annotation (id) ON DELETE CASCADE,
  UNIQUE (link, target)
);

CREATE TABLE text_annotation_data (
  annotation BIGINT NOT NULL REFERENCES text_annotation (id) ON DELETE CASCADE,
  name BIGINT NOT NULL REFERENCES text_qname (id),
  value VARCHAR(255) NOT NULL,
  UNIQUE (annotation, name)
);

CREATE TABLE text_annotation_link_data (
  link BIGINT NOT NULL REFERENCES text_annotation_link (id) ON DELETE CASCADE,
  name BIGINT NOT NULL REFERENCES text_qname (id),
  value VARCHAR(255) NOT NULL,
  UNIQUE (link, name)
);
