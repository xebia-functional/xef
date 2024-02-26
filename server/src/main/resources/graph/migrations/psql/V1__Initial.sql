-- Add Vector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Create collections table
CREATE TABLE IF NOT EXISTS xef_collections (
       uuid TEXT PRIMARY KEY,
       name TEXT UNIQUE NOT NULL
     );

-- Create memory table
CREATE TABLE IF NOT EXISTS xef_memory (
       uuid TEXT PRIMARY KEY,
       conversation_id TEXT NOT NULL,
       role TEXT NOT NULL,
       content TEXT NOT NULL,
       index INT NOT NULL
     );

-- Create embeddings table
CREATE TABLE IF NOT EXISTS xef_embeddings (
       uuid TEXT PRIMARY KEY,
       collection_id TEXT REFERENCES xef_collections(uuid),
       embedding vector(1536),
       content TEXT
     );

-- Add Age extension
CREATE EXTENSION IF NOT EXISTS age;
LOAD 'age';
SET search_path = ag_catalog, "postgres", public;
GRANT USAGE ON SCHEMA ag_catalog TO postgres;
