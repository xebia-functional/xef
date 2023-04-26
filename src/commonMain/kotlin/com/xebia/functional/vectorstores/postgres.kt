package com.xebia.functional.vectorstores

val createCollections = """CREATE TABLE langchain4k_collections (
       uuid TEXT PRIMARY KEY,
       name TEXT UNIQUE NOT NULL
     );""".trimIndent()

val createEmbeddings = """CREATE TABLE langchain4s_embeddings (
       uuid TEXT PRIMARY KEY,
       collection_id TEXT REFERENCES langchain4s_collections(uuid),
       embedding BLOB,
       content TEXT
     );""".trimIndent()

val addVectorExtension = "CREATE EXTENSION IF NOT EXISTS vector;"

val createCollectionsTable = """CREATE TABLE IF NOT EXISTS langchain4s_collections (
       uuid TEXT PRIMARY KEY,
       name TEXT UNIQUE NOT NULL
     );""".trimIndent()

val createEmbeddingTable = """CREATE TABLE IF NOT EXISTS langchain4s_embeddings (
       uid TEXT PRIMARY KEY,
       ollection_id TEXT REFERENCES langchain4s_collections(uuid),
       mbedding BLOB,
       ontent TEXT
     );""".trimIndent()

val addNewCollection = """INSERT INTO langchain4s_collections(uuid, name)
     VALUES (?, ?)
     ON CONFLICT DO NOTHING;""".trimIndent()

val deleteCollection = """DELETE FROM langchain4s_collections
     WHERE uuid = ?;""".trimIndent()

val getCollection = """SELECT * FROM langchain4s_collections
     WHERE name = ?;""".trimIndent()

val getCollectionById = """SELECT * FROM langchain4s_collections
     WHERE uuid = ?;""".trimIndent()

val addNewDocument = """INSERT INTO langchain4s_embeddings(uuid, collection_id, embedding, content)
     VALUES (?, ?, ?, ?);""".trimIndent()

val deleteCollectionDocs = """DELETE FROM langchain4s_embeddings
     WHERE collection_id = ?;""".trimIndent()

val addNewText = """INSERT INTO langchain4s_embeddings(uuid, collection_id, embedding, content)
     VALUES (?, ?, ?, ?);""".trimIndent()

val searchSimilarDocument = """SELECT content FROM langchain4s_embeddings
     WHERE collection_id = ?
     ORDER BY embedding || ?::vector
     LIMIT ?;""".trimIndent()
