package com.xebia.functional.vectorstores

import kotlinx.uuid.UUID

data class PGCollection(val uuid: UUID, val collectionName: String)

enum class PGDistanceStrategy(val strategy: String) {
  Euclidean("<->"), InnerProduct("<#>"), CosineDistance("<=>")
}

val createCollections: String =
  """CREATE TABLE langchain4k_collections (
       uuid TEXT PRIMARY KEY,
       name TEXT UNIQUE NOT NULL
     );""".trimIndent()

val createEmbeddings: String =
  """CREATE TABLE langchain4k_embeddings (
       uuid TEXT PRIMARY KEY,
       collection_id TEXT REFERENCES langchain4k_collections(uuid),
       embedding BLOB,
       content TEXT
     );""".trimIndent()

val addVectorExtension: String =
  "CREATE EXTENSION IF NOT EXISTS vector;"

val createCollectionsTable: String =
  """CREATE TABLE IF NOT EXISTS langchain4k_collections (
       uuid TEXT PRIMARY KEY,
       name TEXT UNIQUE NOT NULL
     );""".trimIndent()

fun createEmbeddingTable(vectorSize: Int): String =
  """CREATE TABLE IF NOT EXISTS langchain4k_embeddings (
       uuid TEXT PRIMARY KEY,
       collection_id TEXT REFERENCES langchain4k_collections(uuid),
       embedding vector($vectorSize),
       content TEXT
     );""".trimIndent()

val addNewCollection: String =
  """INSERT INTO langchain4k_collections(uuid, name)
     VALUES (?, ?)
     ON CONFLICT DO NOTHING;""".trimIndent()

val deleteCollection: String =
  """DELETE FROM langchain4k_collections
     WHERE uuid = ?;""".trimIndent()

val getCollection: String =
  """SELECT * FROM langchain4k_collections
     WHERE name = ?;""".trimIndent()

val getCollectionById: String =
  """SELECT * FROM langchain4k_collections
     WHERE uuid = ?;""".trimIndent()

val addNewDocument: String =
  """INSERT INTO langchain4k_embeddings(uuid, collection_id, embedding, content)
     VALUES (?, ?, ?, ?);""".trimIndent()

val deleteCollectionDocs: String =
  """DELETE FROM langchain4k_embeddings
     WHERE collection_id = ?;""".trimIndent()

val addNewText: String =
  """INSERT INTO langchain4k_embeddings(uuid, collection_id, embedding, content)
     VALUES (?, ?, ?::vector, ?);""".trimIndent()

fun searchSimilarDocument(distance: PGDistanceStrategy): String =
  """SELECT content FROM langchain4k_embeddings
     WHERE collection_id = ?
     ORDER BY embedding
     ${distance.strategy} ?::vector
     LIMIT ?;""".trimIndent()
