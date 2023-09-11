CREATE TABLE IF NOT EXISTS users(
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    email VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(50),
    salt VARCHAR(20),
    auth_token VARCHAR(128) UNIQUE
);

CREATE TABLE IF NOT EXISTS organizations(
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    owner_id INT,

    CONSTRAINT fk_user_id
        FOREIGN KEY (owner_id)
            REFERENCES users(id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS projects(
    id SERIAL PRIMARY KEY,
    name VARCHAR(20),
    org_id INT,

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
    user_id INT,
    project_id INT,
    name VARCHAR(20) NOT NULL,
    token VARCHAR(20) UNIQUE,
    providers_config JSONB,

    PRIMARY KEY (user_id, project_id),

    CONSTRAINT fk_user_id
            FOREIGN KEY (user_id)
                REFERENCES users(id) MATCH SIMPLE
                    ON UPDATE NO ACTION ON DELETE CASCADE,

    CONSTRAINT fk_project_id
            FOREIGN KEY (project_id)
                REFERENCES projects(id) MATCH SIMPLE
                    ON UPDATE NO ACTION ON DELETE CASCADE
);
