//package com.xebia.functional.vectorstores
//
//import com.xebia.functional.Document
//import com.xebia.functional.embeddings.Embedding
//import kotlin.math.sqrt
//
//class InMemoryVectorStore : VectorStore {
//
//  private val documents = mutableMapOf<DocumentVectorId, DocumentVectorId>()
//
//  override fun addTexts(texts: List<String>): List<DocumentVectorId> {
//    val documentVectors = texts.map { embeddings.embedText(it) }
//    val ids = documentVectors.indices.map { it + 1 }.toList()
//    documents.putAll(ids.zip(documentVectors).toMap())
//    return ids
//  }
//
//  override fun addDocuments(documents: List<Document>): List<DocumentVectorId> {
//    val documentVectors = documents.map { embeddings.embedText(it.content) }
//    val ids = documentVectors.indices.map { it + 1 }.toList()
//    this.documents.putAll(ids.zip(documentVectors).toMap())
//    return ids
//  }
//
//  override fun similaritySearch(query: String, limit: Int): List<Document> {
//    val queryVector = embeddings.embedText(query)
//    val results = documents.toList().map { (id, vector) ->
//      Pair(id, cosineSimilarity(vector, queryVector))
//    }.sortedByDescending { it.second }.take(limit)
//    return results.map { Document(it.first.toString(), "") }
//  }
//
//  override fun similaritySearchByVector(embedding: Embedding, k: Int): List<Document> {
//    val results = documents.map { (id, vector) ->
//      Pair(id, cosineSimilarity(vector, embedding.data))
//    }.sortedByDescending { it.second }.take(k)
//    return results.map { Document(it.first.toString(), "") }
//  }
//
//  private fun cosineSimilarity(v1: List<DocumentVectorId>, v2: List<DocumentVectorId>): Float {
//    val freq1 = v1.groupingBy { it.id }.eachCount()
//    val freq2 = v2.groupingBy { it.id }.eachCount()
//
//    val dotProduct = freq1.filterKeys { freq2.containsKey(it) }
//      .map { it.value * freq2.getValue(it.key) }
//      .sum()
//
//    val magnitude1 = sqrt(freq1.values.sumOf { it * it }.toDouble()).toFloat()
//    val magnitude2 = sqrt(freq2.values.sumOf { it * it }.toDouble()).toFloat()
//
//    return if (magnitude1 == 0f || magnitude2 == 0f) 0f
//    else dotProduct / (magnitude1 * magnitude2)
//  }
//}
