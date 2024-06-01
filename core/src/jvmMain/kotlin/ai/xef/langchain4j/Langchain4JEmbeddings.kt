package ai.xef.langchain4j

import ai.xef.Embeddings
import com.xebia.functional.xef.llm.Embedding
import com.xebia.functional.xef.llm.EmbeddingRequest
import com.xebia.functional.xef.llm.EmbeddingResponse
import com.xebia.functional.xef.llm.Usage
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel

abstract class Langchain4JEmbeddings(
  val embeddingModel: EmbeddingModel,
  override val modelName: String,
) : Embeddings {
  override suspend fun createEmbedding(embeddingsRequest: EmbeddingRequest): EmbeddingResponse {
    val response = embeddingModel.embedAll(embeddingsRequest.text.map {
      TextSegment.from(it)
    })
    val embeddings = response.content().map {
      Embedding(it.vectorAsList())
    }
    val usage = response.tokenUsage().let {
      Usage(it.inputTokenCount(), it.outputTokenCount(), it.totalTokenCount())
    }
    return EmbeddingResponse(
      embedding = embeddings,
      usage = usage
    )
  }
}
