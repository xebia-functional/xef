CREATE TABLE IF NOT EXISTS xef_assistants(
    id UUID PRIMARY KEY,
    data JSONB NOT NULL
);

CREATE TABLE IF NOT EXISTS xef_assistants_files(
    id UUID PRIMARY KEY,
    data JSONB NOT NULL
);

CREATE TABLE IF NOT EXISTS xef_messages(
    id UUID PRIMARY KEY,
    data JSONB NOT NULL
);

CREATE TABLE IF NOT EXISTS xef_messages_files(
    id UUID PRIMARY KEY,
    thread_id UUID NOT NULL,
    data JSONB NOT NULL
);

CREATE TABLE IF NOT EXISTS xef_runs(
    id UUID PRIMARY KEY,
    data JSONB NOT NULL
);

CREATE TABLE IF NOT EXISTS xef_threads(
    id UUID PRIMARY KEY,
    data JSONB NOT NULL
);
