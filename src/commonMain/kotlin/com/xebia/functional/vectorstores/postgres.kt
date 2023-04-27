package com.xebia.functional.vectorstores

import kotlinx.uuid.UUID

data class PGCollection(val uuid: UUID, val collectionName: String)

enum class PGDistanceStrategy(val strategy: String) {
  Euclidean("<->"), InnerProduct("<#>"), CosineDistance("<=>")
}

val createCollections = """CREATE TABLE langchain4k_collections (
       uuid TEXT PRIMARY KEY,
       name TEXT UNIQUE NOT NULL
     );""".trimIndent()

val createEmbeddings = """CREATE TABLE langchain4k_embeddings (
       uuid TEXT PRIMARY KEY,
       collection_id TEXT REFERENCES langchain4k_collections(uuid),
       embedding BLOB,
       content TEXT
     );""".trimIndent()

val addVectorExtension = "CREATE EXTENSION IF NOT EXISTS vector;"

val createCollectionsTable = """CREATE TABLE IF NOT EXISTS langchain4k_collections (
       uuid TEXT PRIMARY KEY,
       name TEXT UNIQUE NOT NULL
     );""".trimIndent()

fun createEmbeddingTable(vectorSize: Int) = """CREATE TABLE IF NOT EXISTS langchain4k_embeddings (
       uuid TEXT PRIMARY KEY,
       collection_id TEXT REFERENCES langchain4k_collections(uuid),
       embedding vector($vectorSize),
       content TEXT
     );""".trimIndent()

val addNewCollection = """INSERT INTO langchain4k_collections(uuid, name)
     VALUES (?, ?)
     ON CONFLICT DO NOTHING;""".trimIndent()

val deleteCollection = """DELETE FROM langchain4k_collections
     WHERE uuid = ?;""".trimIndent()

val getCollection = """SELECT * FROM langchain4k_collections
     WHERE name = ?;""".trimIndent()

val getCollectionById = """SELECT * FROM langchain4k_collections
     WHERE uuid = ?;""".trimIndent()

val addNewDocument = """INSERT INTO langchain4k_embeddings(uuid, collection_id, embedding, content)
     VALUES (?, ?, ?, ?);""".trimIndent()

val deleteCollectionDocs = """DELETE FROM langchain4k_embeddings
     WHERE collection_id = ?;""".trimIndent()

val addNewText = """INSERT INTO langchain4k_embeddings(uuid, collection_id, embedding, content)
     VALUES (?, ?, ?::vector, ?);""".trimIndent()

fun searchSimilarDocument(distance: PGDistanceStrategy) = """SELECT content FROM langchain4k_embeddings
     WHERE collection_id = ?
     ORDER BY embedding
     ${distance.strategy} ?::vector
     LIMIT ?;""".trimIndent()
