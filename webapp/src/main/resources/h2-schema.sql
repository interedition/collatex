CREATE TABLE IF NOT EXISTS text_metadata (
  text BIGINT NOT NULL REFERENCES text_content (id) ON DELETE CASCADE,
  text_created TIMESTAMP NOT NULL,
  text_updated TIMESTAMP NOT NULL,
  text_title TEXT,
  text_creator TEXT,
  text_subject TEXT,
  text_description TEXT,
  text_publisher TEXT,
  text_contributor TEXT,
  text_date TEXT,
  text_type TEXT,
  text_format TEXT,
  text_identifier TEXT,
  text_source TEXT,
  text_language TEXT,
  UNIQUE (text)
);

CREATE TABLE IF NOT EXISTS xml_transform (
  id BIGINT IDENTITY,
  name VARCHAR(128) NOT NULL,
  transform_tei BOOLEAN NOT NULL DEFAULT TRUE,
  compress_space BOOLEAN NOT NULL DEFAULT TRUE,
  remove_empty BOOLEAN NOT NULL DEFAULT TRUE,
  notable_char CHAR(2) NOT NULL DEFAULT '§',
  description TEXT NULL,
  UNIQUE(name)
);

CREATE TABLE IF NOT EXISTS xml_transform_rule (
  config BIGINT NOT NULL REFERENCES xml_transform (id) ON DELETE CASCADE,
  name BIGINT NOT NULL REFERENCES text_qname (id),
  is_line BOOLEAN NOT NULL,
  is_container BOOLEAN NOT NULL,
  is_included BOOLEAN NOT NULL,
  is_excluded BOOLEAN NOT NULL,
  is_notable BOOLEAN NOT NULL,
  UNIQUE(config, name)
);