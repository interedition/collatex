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