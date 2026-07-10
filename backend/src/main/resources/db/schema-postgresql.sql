CREATE TABLE users
(
    id        SERIAL PRIMARY KEY,

    email     VARCHAR(255) NOT NULL UNIQUE,

    username VARCHAR(255) UNIQUE,

    role      VARCHAR(50)  NOT NULL DEFAULT 'USER'
);

CREATE TABLE linked_accounts
(
    id      SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL
        REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE TABLE form_accounts
(
    id            INTEGER PRIMARY KEY
        REFERENCES linked_accounts (id)
            ON DELETE CASCADE,

    password_hash TEXT NOT NULL
);

CREATE TABLE oauth_accounts
(
    id            INTEGER PRIMARY KEY
        REFERENCES linked_accounts (id)
            ON DELETE CASCADE,

    access_token  TEXT,
    refresh_token TEXT,

    provider      VARCHAR(100) NOT NULL,
    provider_id   VARCHAR(255) NOT NULL
);

CREATE TABLE scheduled_reports
(
    id                  SERIAL PRIMARY KEY,

    report_type         VARCHAR(31) NOT NULL,

    user_id             INTEGER     NOT NULL
        REFERENCES users (id)
            ON DELETE CASCADE,

    git_account_id      INTEGER
        REFERENCES oauth_accounts
            ON DELETE CASCADE,

    repo_uri            TEXT        NOT NULL,

    next_run            TIMESTAMPTZ,
    last_run            TIMESTAMPTZ,

    data_start          TIMESTAMPTZ NOT NULL,

    is_cancelled        BOOLEAN     NOT NULL DEFAULT FALSE,

    cancellation_reason VARCHAR(100),

    llm_complexity      VARCHAR(20),
    llm_mode            VARCHAR(10),
    llm_analyses        VARCHAR(500)
);

ALTER TABLE scheduled_reports
    ADD CONSTRAINT chk_scheduled_reports_type
        CHECK (report_type IN ('ONE_TIME', 'PERIODIC'));

CREATE TABLE one_time_scheduled_report_entity
(
    id INTEGER PRIMARY KEY
        REFERENCES scheduled_reports (id)
            ON DELETE CASCADE
);

CREATE TABLE periodic_scheduled_report_entity
(
    id              INTEGER PRIMARY KEY
        REFERENCES scheduled_reports (id)
            ON DELETE CASCADE,

    active          BOOLEAN      NOT NULL DEFAULT TRUE,

    timezone        VARCHAR(100) NOT NULL,

    cron_expression VARCHAR(255) NOT NULL
);

CREATE TABLE scheduled_report_jobs
(
    id                  SERIAL PRIMARY KEY,

    scheduled_report_id INTEGER     NOT NULL
        REFERENCES scheduled_reports (id)
            ON DELETE CASCADE,

    data_from           TIMESTAMPTZ NOT NULL,

    scheduled_for       TIMESTAMPTZ NOT NULL,

    retry_count         INTEGER     NOT NULL DEFAULT 0,

    status               VARCHAR(20) NOT NULL,

    run_at              TIMESTAMPTZ,
    started_at          TIMESTAMPTZ,
    ended_at            TIMESTAMPTZ,

    error_msg           TEXT,

    CONSTRAINT chk_job_state
        CHECK (status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILURE'))
);

CREATE TABLE report
(
    id         SERIAL PRIMARY KEY,

    user_id    INTEGER      NOT NULL REFERENCES users (id),

    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    analysis   JSONB        NOT NULL,

    pdf        BYTEA
);