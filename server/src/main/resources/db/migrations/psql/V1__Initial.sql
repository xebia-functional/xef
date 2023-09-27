CREATE TABLE IF NOT EXISTS users(
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    email VARCHAR(50) UNIQUE NOT NULL,
    password_hash BYTEA NOT NULL,
    salt BYTEA NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    auth_token VARCHAR(128) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS organizations(
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    owner_id INT NOT NULL,

    CONSTRAINT fk_user_id
        FOREIGN KEY (owner_id)
            REFERENCES users(id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS projects(
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    org_id INT NOT NULL,

    CONSTRAINT fk_org_id
        FOREIGN KEY (org_id)
            REFERENCES organizations(id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS users_org(
    user_id INT,
    org_id INT,

    PRIMARY KEY (user_id, org_id),

    CONSTRAINT fk_user_id
        FOREIGN KEY (user_id)
            REFERENCES users(id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE CASCADE,

    CONSTRAINT fk_org_id
            FOREIGN KEY (org_id)
                REFERENCES organizations(id) MATCH SIMPLE
                    ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS xef_tokens(
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    project_id INT NOT NULL,
    name VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    token VARCHAR(128) UNIQUE,
    providers_config JSONB,

    CONSTRAINT fk_user_id
            FOREIGN KEY (user_id)
                REFERENCES users(id) MATCH SIMPLE
                    ON UPDATE NO ACTION ON DELETE CASCADE,

    CONSTRAINT fk_project_id
            FOREIGN KEY (project_id)
                REFERENCES projects(id) MATCH SIMPLE
                    ON UPDATE NO ACTION ON DELETE CASCADE
);
