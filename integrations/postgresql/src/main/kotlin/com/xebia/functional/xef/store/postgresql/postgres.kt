package com.xebia.functional.xef.store.postgresql

import kotlinx.uuid.UUID

data class PGCollection(val uuid: UUID, val collectionName: String)

enum class PGDistanceStrategy(val strategy: String) {
  Euclidean("<->"),
  InnerProduct("<#>"),
  CosineDistance("<=>")
}

val createMemoryTable: String =
  """CREATE TABLE IF NOT EXISTS xef_memory (
       uuid TEXT PRIMARY KEY,
       conversation_id TEXT NOT NULL,
       role TEXT NOT NULL,
       content TEXT NOT NULL,
       index INT NOT NULL
     );"""
    .trimIndent()

val createEmbeddings: String =
  """CREATE TABLE xef_embeddings (
       uuid TEXT PRIMARY KEY,
       collection_id TEXT REFERENCES xef_collections(uuid),
       embedding BLOB,
       content TEXT
     );"""
    .trimIndent()

val addVectorExtension: String = "CREATE EXTENSION IF NOT EXISTS vector;"

val createCollectionsTable: String =
  """CREATE TABLE IF NOT EXISTS xef_collections (
       uuid TEXT PRIMARY KEY,
       name TEXT UNIQUE NOT NULL
     );"""
    .trimIndent()

fun createEmbeddingTable(vectorSize: Int): String =
  """CREATE TABLE IF NOT EXISTS xef_embeddings (
       uuid TEXT PRIMARY KEY,
       collection_id TEXT REFERENCES xef_collections(uuid),
       embedding vector($vectorSize),
       content TEXT
     );"""
    .trimIndent()

val addNewMemory: String =
  """INSERT INTO xef_memory(uuid, conversation_id, role, content, index)
     VALUES (?, ?, ?, ?, ?)
     ON CONFLICT DO NOTHING;"""
    .trimIndent()

val addNewCollection: String =
  """INSERT INTO xef_collections(uuid, name)
     VALUES (?, ?)
     ON CONFLICT DO NOTHING;"""
    .trimIndent()

val deleteCollection: String =
  """DELETE FROM xef_collections
     WHERE uuid = ?;"""
    .trimIndent()

val deleteMemory: String =
  """DELETE FROM xef_memory
     WHERE uuid = ?;"""
    .trimIndent()

val getCollection: String =
  """SELECT * FROM xef_collections
     WHERE name = ?;"""
    .trimIndent()

val getCollectionById: String =
  """SELECT * FROM xef_collections
     WHERE uuid = ?;"""
    .trimIndent()

val getMemoriesByConversationId: String =
    """
    SELECT
        uuid,
        conversation_id,
        role,
        content,
        index
    FROM
        xef_memory
    WHERE
        conversation_id = ?
    ORDER BY index DESC;
    """.trimIndent()

val addNewDocument: String =
  """INSERT INTO xef_embeddings(uuid, collection_id, embedding, content)
     VALUES (?, ?, ?, ?);"""
    .trimIndent()

val deleteCollectionDocs: String =
  """DELETE FROM xef_embeddings
     WHERE collection_id = ?;"""
    .trimIndent()

val addNewText: String =
  """INSERT INTO xef_embeddings(uuid, collection_id, embedding, content)
     VALUES (?, ?, ?::vector, ?);"""
    .trimIndent()

fun searchSimilarDocument(distance: PGDistanceStrategy): String =
  """SELECT content FROM xef_embeddings
     WHERE collection_id = ?
     ORDER BY embedding
     ${distance.strategy} ?::vector
     LIMIT ?;"""
    .trimIndent()
