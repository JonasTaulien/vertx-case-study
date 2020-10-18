SET timezone = 'UTC';

CREATE TABLE IF NOT EXISTS headline
(
    id           SERIAL PRIMARY KEY,
    source       VARCHAR,
    author       VARCHAR,
    title        VARCHAR,
    description  VARCHAR,
    published_at timestamptz
);

CREATE TABLE IF NOT EXISTS "user"
(
    id       SERIAL PRIMARY KEY,
    email    VARCHAR,
    password VARCHAR
)