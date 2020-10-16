SET timezone = 'UTC';

CREATE TABLE IF NOT EXISTS headline
(
    source VARCHAR,
    author VARCHAR,
    title VARCHAR,
    description VARCHAR,
    published_at timestamptz
);
