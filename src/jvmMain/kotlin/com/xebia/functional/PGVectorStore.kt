package com.xebia.functional

import com.xebia.functional.embeddings.Embedding
import com.xebia.functional.embeddings.Embeddings
import com.xebia.functional.llm.openai.RequestConfig
import com.xebia.functional.vectorstores.DocumentVectorId
import com.xebia.functional.vectorstores.PGCollection
import com.xebia.functional.vectorstores.PGDistanceStrategy
import com.xebia.functional.vectorstores.VectorStore
import com.xebia.functional.vectorstores.addNewCollection
import com.xebia.functional.vectorstores.addNewText
import com.xebia.functional.vectorstores.addVectorExtension
import com.xebia.functional.vectorstores.createCollectionsTable
import com.xebia.functional.vectorstores.createEmbeddingTable
import com.xebia.functional.vectorstores.deleteCollection
import com.xebia.functional.vectorstores.deleteCollectionDocs
import com.xebia.functional.vectorstores.getCollection
import com.xebia.functional.vectorstores.searchSimilarDocument
import javax.sql.DataSource
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class PGVectorStore(
  private val vectorSize: Int,
  private val dataSource: DataSource,
  private val embeddings: Embeddings,
  private val collectionName: String,
  private val distanceStrategy: PGDistanceStrategy,
  private val preDeleteCollection: Boolean,
  private val requestConfig: RequestConfig,
  private val chunckSize: Int?
) : VectorStore {

  suspend fun JDBCSyntax.getCollection(collectionName: String): PGCollection =
    queryOneOrNull(getCollection,
      { bind(collectionName) }
    ) { PGCollection(UUID(string()), string()) }
      ?: throw IllegalStateException("Collection '$collectionName' not found")

  suspend fun JDBCSyntax.deleteCollection() {
    if (preDeleteCollection) {
      val collection = getCollection(collectionName)
      update(deleteCollectionDocs) { bind(collection.uuid.toString()) }
      update(deleteCollection) { bind(collection.uuid.toString()) }
    }
  }

  suspend fun initialDbSetup(): Unit = dataSource.connection {
    update(addVectorExtension)
    update(createCollectionsTable)
    update(createEmbeddingTable(vectorSize))
    deleteCollection()
  }

  suspend fun createCollection(): Unit = dataSource.connection {
    val xa = UUID.generateUUID()
    update(addNewCollection) { bind(xa.toString()); bind(collectionName) }
  }

  override suspend fun addTexts(texts: List<String>): List<DocumentVectorId> = dataSource.connection {
    val embeddings = embeddings.embedDocuments(texts, chunckSize, requestConfig)
    val collection = getCollection(collectionName)
    texts.zip(embeddings) { text, embedding ->
      val uuid = UUID.generateUUID()
      update(addNewText) {
        bind(uuid.toString())
        bind(collection.uuid.toString())
        bind(embedding.data.toString())
        bind(text)
      }
      DocumentVectorId(uuid)
    }
  }

  override suspend fun addDocuments(documents: List<Document>): List<DocumentVectorId> =
    addTexts(documents.map(Document::content))

  override suspend fun similaritySearch(query: String, limit: Int): List<Document> = dataSource.connection {
    val embeddings = embeddings.embedQuery(query, requestConfig).ifEmpty { throw IllegalStateException("Embedding for text: '$query', has not been properly generated") }
    val collection = getCollection(collectionName)
    queryAsList(searchSimilarDocument(distanceStrategy), {
      bind(collection.uuid.toString())
      bind(embeddings[0].data.toString())
      bind(limit)
    }) { Document(string()) }
  }

  override suspend fun similaritySearchByVector(embedding: Embedding, limit: Int): List<Document> =
    dataSource.connection {
      val collection = getCollection(collectionName)
      queryAsList(searchSimilarDocument(distanceStrategy), {
        bind(collection.uuid.toString())
        bind(embedding.data.toString())
        bind(limit)
      }) { Document(string()) }
    }
}
