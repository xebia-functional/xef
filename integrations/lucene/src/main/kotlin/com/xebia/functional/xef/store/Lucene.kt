package com.xebia.functional.xef.store

import ai.xef.openai.OpenAIModel
import arrow.atomic.AtomicInt
import com.xebia.functional.openai.apis.EmbeddingsApi
import com.xebia.functional.openai.models.ChatCompletionRole
import com.xebia.functional.openai.models.Embedding
import com.xebia.functional.xef.llm.embedQuery
import com.xebia.functional.xef.llm.models.modelType
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.*
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.VectorSimilarityFunction
import org.apache.lucene.queries.mlt.MoreLikeThis
import org.apache.lucene.search.*
import org.apache.lucene.store.Directory
import org.apache.lucene.store.MMapDirectory
import java.io.StringReader
import java.nio.file.Path

open class Lucene(
  private val writer: IndexWriter,
  private val embeddings: EmbeddingsApi?,
  private val similarity: VectorSimilarityFunction = VectorSimilarityFunction.EUCLIDEAN,
) : VectorStore, AutoCloseable {

  override val indexValue: AtomicInt = AtomicInt(0)

  override fun updateIndexByConversationId(conversationId: ConversationId) {
    getMemoryByConversationId(conversationId).firstOrNull()?.let { indexValue.set(it.index) }
  }

  override suspend fun addMemories(memories: List<Memory>) {
    memories.forEach {
      val doc =
        Document().apply {
          add(TextField("conversationId", it.conversationId.value, Field.Store.YES))
          add(TextField("content", it.content.asRequestMessage().contentAsString(), Field.Store.YES))
          add(TextField("role", it.content.role.name.lowercase(), Field.Store.YES))
          add(IntField("index", it.index, Field.Store.YES))
        }
      writer.addDocument(doc)
    }
    writer.commit()
  }

  override suspend fun <T> memories(
    model: OpenAIModel<T>, conversationId: ConversationId, limitTokens: Int): List<Memory> =
    getMemoryByConversationId(conversationId).reduceByLimitToken(model.modelType(), limitTokens).reversed()

  override suspend fun addTexts(texts: List<String>) {
    texts.forEach {
      val embedding = embeddings?.embedQuery(it)
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
    val luceneQuery = KnnFloatVectorQuery("embedding", embedding.embedding.map { it.toFloat() }.toFloatArray(), limit)
    val searcher = IndexSearcher(DirectoryReader.open(writer))
    return searcher.search(luceneQuery, limit).extract(searcher)
  }

  private fun List<ScoreDoc>.extractMemory(searcher: IndexSearcher): List<Memory> =
    map {
      val doc = searcher.storedFields().document(it.doc)
      val role = ChatCompletionRole.valueOf(doc.get("role").lowercase())
      val content = doc.get("content")
      Memory(
        conversationId = ConversationId(doc.get("conversationId")),
        content = memorizedMessage(role, content),
        index = doc.get("index").toInt()
      )
    }

  private fun TopDocs.extract(searcher: IndexSearcher): List<String> =
    scoreDocs.map {
      searcher.storedFields().document(it.doc).get("contents")
    }

  override fun close() {
    writer.close()
  }

  private fun getMemoryByConversationId(conversationId: ConversationId): List<Memory> {
    val reader = DirectoryReader.open(writer)
    val mlt = MoreLikeThis(reader)
    mlt.analyzer = StandardAnalyzer()
    mlt.minTermFreq = 1
    mlt.minDocFreq = 1
    mlt.minWordLen = 3
    val sort = Sort(SortField("index", SortField.Type.LONG, true))
    val luceneQuery = mlt.like("conversationId", StringReader(conversationId.value))
    val searcher = IndexSearcher(reader)

    val docs = IndexSearcher(reader).search(luceneQuery, reader.numDocs(), sort)

    return docs.scoreDocs.toList().extractMemory(searcher)
  }

}

class DirectoryLucene(
  private val directory: Directory,
  writerConfig: IndexWriterConfig = IndexWriterConfig(),
  embeddings: EmbeddingsApi?,
  similarity: VectorSimilarityFunction = VectorSimilarityFunction.EUCLIDEAN
) : Lucene(IndexWriter(directory, writerConfig), embeddings, similarity) {
  override fun close() {
    super.close()
    directory.close()
  }
}

@JvmOverloads
fun InMemoryLucene(
  path: Path,
  writerConfig: IndexWriterConfig = IndexWriterConfig(),
  embeddings: EmbeddingsApi?,
  similarity: VectorSimilarityFunction = VectorSimilarityFunction.EUCLIDEAN
): DirectoryLucene = DirectoryLucene(MMapDirectory(path), writerConfig, embeddings, similarity)

@JvmOverloads
fun InMemoryLuceneBuilder(
  path: Path,
  useAIEmbeddings: Boolean = true,
  writerConfig: IndexWriterConfig = IndexWriterConfig(),
  similarity: VectorSimilarityFunction = VectorSimilarityFunction.EUCLIDEAN
): (EmbeddingsApi) -> DirectoryLucene = { embeddings ->
  InMemoryLucene(path, writerConfig, embeddings.takeIf { useAIEmbeddings }, similarity)
}

fun List<Embedding>.toFloatArray(): FloatArray = flatMap { it.embedding.map { it.toFloat() } }.toFloatArray()

