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
       request JSONB,
       aiResponse JSONB,
       timestamp BIGINT NOT NULL,
       responseTimeInMillis INT NOT NULL,
       tokens INT NOT NULL
     );

-- Create embeddings table
CREATE TABLE IF NOT EXISTS xef_embeddings (
       uuid TEXT PRIMARY KEY,
       collection_id TEXT REFERENCES xef_collections(uuid),
       embedding vector(3),
       content TEXT
     );

