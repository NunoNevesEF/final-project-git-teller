CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    user_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'USER'
);

CREATE TABLE IF NOT EXISTS linked_accounts (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    provider_id VARCHAR(255),
    password_hash TEXT,
    access_token TEXT,
    refresh_token TEXT,
    CONSTRAINT accounts_type CHECK (type IN ('form', 'google', 'github')),
    CONSTRAINT account_requires_password CHECK (
        (type = 'form' AND password_hash IS NOT NULL)
        OR (type <> 'form')
    )
);
