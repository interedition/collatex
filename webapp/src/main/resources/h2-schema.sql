CREATE TABLE IF NOT EXISTS repository_text_collection (
  id BIGINT IDENTITY,
  name VARCHAR(255) NOT NULL,
  UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS repository_text_metadata (
  text BIGINT NOT NULL REFERENCES text_content (id) ON DELETE CASCADE,
  created TIMESTAMP NOT NULL,
  updated TIMESTAMP NOT NULL,
  collection BIGINT REFERENCES repository_text_collection (id) ON DELETE CASCADE,
  title VARCHAR(255),
  summary VARCHAR(255),
  author VARCHAR(255),
  UNIQUE (text, collection)
);
