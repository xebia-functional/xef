package com.xebia.functional.vectorstores

import com.xebia.functional.domain.Document
import com.xebia.functional.embeddings.Embeddings
import com.xebia.functional.embeddings.models.Embedding
import com.xebia.functional.vectorstores.models.DocumentVectorId

trait VectorStore[F[_]]:
  /**
   * Add texts to the vector store after running them through the embeddings
   *
   * @param texts
   *   list of text to add to the vector store
   * @return
   *   a list of IDs from adding the texts to the vector store
   */
  def addTexts(texts: List[String]): F[List[DocumentVectorId]]

  /**
   * Add documents to the vector store after running them through the embeddings
   *
   * @param documents
   *   list of Documents to add to the vector store
   * @return
   *   a list of IDs from adding the documents to the vector store
   */
  def addDocuments(documents: List[Document]): F[List[DocumentVectorId]]

  /**
   * Return the docs most similar to the query
   *
   * @param query
   *   text to use to search for similar documents
   * @param k
   *   number of documents to return
   * @return
   *   a list of Documents most similar to query
   */
  def similaritySearch(query: String, k: Int): F[List[Document]]

  /**
   * Return the docs most similar to the embedding
   *
   * @param embedding
   *   embedding vector to use to search for similar documents
   * @param k
   *   number of documents to return
   * @return
   *   list of Documents most similar to the embedding
   */
  def similaritySearchByVector(embedding: Embedding, k: Int): F[List[Document]]
