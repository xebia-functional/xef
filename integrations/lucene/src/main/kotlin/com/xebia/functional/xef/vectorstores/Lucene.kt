package com.xebia.functional.xef.vectorstores

import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.autoCloseable
import com.xebia.functional.xef.embeddings.Embedding
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.llm.openai.EmbeddingModel
import com.xebia.functional.xef.llm.openai.RequestConfig
import java.nio.file.Path
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.KnnFloatVectorField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.*
import org.apache.lucene.search.FuzzyQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.KnnFloatVectorQuery
import org.apache.lucene.search.Query
import org.apache.lucene.store.Directory
import org.apache.lucene.store.MMapDirectory

open class Lucene(
  private val writer: IndexWriter,
  private val searcher: IndexSearcher,
  private val embeddings: Embeddings?,
  private val similarity: VectorSimilarityFunction = VectorSimilarityFunction.EUCLIDEAN
) : VectorStore, AutoCloseable {

  constructor(
    writer: IndexWriter,
    embeddings: Embeddings?,
    similarity: VectorSimilarityFunction = VectorSimilarityFunction.EUCLIDEAN
  ) : this(writer, IndexSearcher(DirectoryReader.open(writer)), embeddings, similarity)

  private val requestConfig =
    RequestConfig(EmbeddingModel.TextEmbeddingAda002, RequestConfig.Companion.User("user"))

  override suspend fun addTexts(texts: List<String>) =
    texts.forEach {
      val embedding = embeddings?.embedQuery(it, requestConfig)
      val doc =
        Document().apply {
          add(TextField("contents", it, Field.Store.YES))
          if (embedding != null) add(KnnFloatVectorField("embedding", embedding.toFloatArray(), similarity))
        }
      writer.addDocument(doc)
    }

  override suspend fun similaritySearch(query: String, limit: Int): List<String> =
    search(FuzzyQuery(Term("contents", query)), limit)

  override suspend fun similaritySearchByVector(embedding: Embedding, limit: Int): List<String> {
    requireNotNull(embeddings) { "no embeddings were computed for this model" }
    return search(KnnFloatVectorQuery("embedding", embedding.data.toFloatArray(), limit), limit)
  }

  private fun search(q: Query, limit: Int): List<String> =
    searcher.search(q, limit).scoreDocs.map {
      searcher.storedFields().document(it.doc).get("contents")
    }

  override fun close() {
    writer.close()
  }
}

class DirectoryLucene(
  private val directory: Directory,
  writerConfig: IndexWriterConfig = IndexWriterConfig(),
  embeddings: Embeddings?,
  similarity: VectorSimilarityFunction = VectorSimilarityFunction.EUCLIDEAN
) : Lucene(IndexWriter(directory, writerConfig), embeddings, similarity) {
  override fun close() {
    super.close()
    directory.close()
  }
}

fun InMemoryLucene(
  path: Path,
  writerConfig: IndexWriterConfig = IndexWriterConfig(),
  embeddings: Embeddings?,
  similarity: VectorSimilarityFunction = VectorSimilarityFunction.EUCLIDEAN
): DirectoryLucene = DirectoryLucene(MMapDirectory(path), writerConfig, embeddings, similarity)

fun InMemoryLuceneBuilder(
  path: Path,
  useAIEmbeddings: Boolean = true,
  writerConfig: IndexWriterConfig = IndexWriterConfig(),
  similarity: VectorSimilarityFunction = VectorSimilarityFunction.EUCLIDEAN
): suspend ResourceScope.(Embeddings) -> DirectoryLucene = { embeddings ->
  autoCloseable { InMemoryLucene(path, writerConfig, embeddings.takeIf { useAIEmbeddings }, similarity) }
}

fun List<Embedding>.toFloatArray(): FloatArray = flatMap { it.data }.toFloatArray()
