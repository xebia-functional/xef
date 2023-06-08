package com.xebia.functional.xef.vectorstores

import com.xebia.functional.xef.embeddings.Embedding
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.llm.openai.EmbeddingModel
import com.xebia.functional.xef.llm.openai.RequestConfig
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.*
import java.nio.file.Path
import org.apache.lucene.index.*
import org.apache.lucene.queries.mlt.MoreLikeThis
import org.apache.lucene.search.*
import org.apache.lucene.store.Directory
import org.apache.lucene.store.MMapDirectory
import java.io.StringReader

open class Lucene(
  private val writer: IndexWriter,
  private val embeddings: Embeddings?,
  private val similarity: VectorSimilarityFunction = VectorSimilarityFunction.EUCLIDEAN
) : VectorStore, AutoCloseable {

  private val requestConfig =
    RequestConfig(EmbeddingModel.TextEmbeddingAda002, RequestConfig.Companion.User("user"))

  override suspend fun addTexts(texts: List<String>) {
    texts.forEach {
      val embedding = embeddings?.embedQuery(it, requestConfig)
      val doc =
        Document().apply {
          add(TextField("contents", it, Field.Store.YES))
          if (embedding != null) add(KnnFloatVectorField("embedding", embedding.toFloatArray(), similarity))
        }
      writer.addDocument(doc)
    }
    writer.commit()
  }

  override suspend fun similaritySearch(query: String, limit: Int): List<String> {
    val reader = DirectoryReader.open(writer)
    val mlt = MoreLikeThis(reader)
    mlt.analyzer = StandardAnalyzer()
    mlt.minTermFreq = 1
    mlt.minDocFreq = 1
    mlt.minWordLen = 3
    val luceneQuery = mlt.like("contents", StringReader(query))
    val searcher = IndexSearcher(reader)
    return IndexSearcher(reader).search(luceneQuery, limit).extract(searcher)
  }

  override suspend fun similaritySearchByVector(embedding: Embedding, limit: Int): List<String> {
    requireNotNull(embeddings) { "no embeddings were computed for this model" }
    val luceneQuery = KnnFloatVectorQuery("embedding", embedding.data.toFloatArray(), limit)
    val searcher = IndexSearcher(DirectoryReader.open(writer))
    return searcher.search(luceneQuery, limit).extract(searcher)
  }

  fun TopDocs.extract(searcher: IndexSearcher): List<String> =
    scoreDocs.map {
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
): suspend (Embeddings) -> DirectoryLucene = { embeddings ->
  InMemoryLucene(path, writerConfig, embeddings.takeIf { useAIEmbeddings }, similarity)
}

fun List<Embedding>.toFloatArray(): FloatArray = flatMap { it.data }.toFloatArray()
