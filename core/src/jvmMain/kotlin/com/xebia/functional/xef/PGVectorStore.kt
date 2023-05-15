package com.xebia.functional.xef

import com.xebia.functional.xef.embeddings.Embedding
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.llm.openai.RequestConfig
import com.xebia.functional.xef.vectorstores.PGCollection
import com.xebia.functional.xef.vectorstores.PGDistanceStrategy
import com.xebia.functional.xef.vectorstores.VectorStore
import com.xebia.functional.xef.vectorstores.addNewCollection
import com.xebia.functional.xef.vectorstores.addNewText
import com.xebia.functional.xef.vectorstores.addVectorExtension
import com.xebia.functional.xef.vectorstores.createCollectionsTable
import com.xebia.functional.xef.vectorstores.createEmbeddingTable
import com.xebia.functional.xef.vectorstores.deleteCollection
import com.xebia.functional.xef.vectorstores.deleteCollectionDocs
import com.xebia.functional.xef.vectorstores.getCollection
import com.xebia.functional.xef.vectorstores.searchSimilarDocument
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
    queryOneOrNull(getCollection, { bind(collectionName) }) {
      PGCollection(UUID(string()), string())
    }
      ?: throw IllegalStateException("Collection '$collectionName' not found")

  suspend fun JDBCSyntax.deleteCollection() {
    if (preDeleteCollection) {
      val collection = getCollection(collectionName)
      update(deleteCollectionDocs) { bind(collection.uuid.toString()) }
      update(deleteCollection) { bind(collection.uuid.toString()) }
    }
  }

  suspend fun initialDbSetup(): Unit =
    dataSource.connection {
      update(addVectorExtension)
      update(createCollectionsTable)
      update(createEmbeddingTable(vectorSize))
      deleteCollection()
    }

  suspend fun createCollection(): Unit =
    dataSource.connection {
      val xa = UUID.generateUUID()
      update(addNewCollection) {
        bind(xa.toString())
        bind(collectionName)
      }
    }

  override suspend fun addTexts(texts: List<String>): Unit =
    dataSource.connection {
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
      }
    }

  override suspend fun similaritySearch(query: String, limit: Int): List<String> =
    dataSource.connection {
      val embeddings =
        embeddings.embedQuery(query, requestConfig).ifEmpty {
          throw IllegalStateException(
            "Embedding for text: '$query', has not been properly generated"
          )
        }
      val collection = getCollection(collectionName)
      queryAsList(
        searchSimilarDocument(distanceStrategy),
        {
          bind(collection.uuid.toString())
          bind(embeddings[0].data.toString())
          bind(limit)
        }
      ) {
        string()
      }
    }

  override suspend fun similaritySearchByVector(embedding: Embedding, limit: Int): List<String> =
    dataSource.connection {
      val collection = getCollection(collectionName)
      queryAsList(
        searchSimilarDocument(distanceStrategy),
        {
          bind(collection.uuid.toString())
          bind(embedding.data.toString())
          bind(limit)
        }
      ) {
        string()
      }
    }
}
