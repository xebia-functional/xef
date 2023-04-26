package com.xebia.functional

import com.xebia.functional.embeddings.Embedding
import com.xebia.functional.vectorstores.DocumentVectorId
import com.xebia.functional.vectorstores.VectorStore
import javax.sql.DataSource

class JDBCVectorStore private constructor(
  val dataSource: DataSource
): VectorStore {
  override fun addTexts(texts: List<String>): List<DocumentVectorId> {
    TODO("Not yet implemented")
  }

  override fun addDocuments(documents: List<Document>): List<DocumentVectorId> {
    TODO("Not yet implemented")
  }

  override fun similaritySearch(query: String, limit: Int): List<Document> {
    TODO("Not yet implemented")
  }

  override fun similaritySearchByVector(embedding: Embedding, k: Int): List<Document> {
    TODO("Not yet implemented")
  }

}