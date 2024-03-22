package com.xebia.functional.xef.gcp

import arrow.core.nonEmptyListOf
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.PlatformConversation
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.gcp.models.GcpChat
import com.xebia.functional.xef.gcp.models.GcpEmbeddings
import com.xebia.functional.xef.llm.LLM
import com.xebia.functional.xef.metrics.Metric
import com.xebia.functional.xef.store.LocalVectorStore
import com.xebia.functional.xef.store.VectorStore
import kotlin.jvm.JvmField
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

private const val GCP_TOKEN_ENV_VAR = "GCP_TOKEN"
private const val GCP_PROJECT_ID_VAR = "GCP_PROJECT_ID"
private const val GCP_LOCATION_VAR = "GCP_LOCATION"

class GCP(projectId: String? = null, location: VertexAIRegion? = null, token: String? = null) {
  private val config =
    GcpConfig(
      token = token ?: tokenFromEnv(),
      projectId = projectId ?: projectIdFromEnv(),
      location = location ?: locationFromEnv(),
    )

  private fun tokenFromEnv(): String = fromEnv(GCP_TOKEN_ENV_VAR)

  private fun projectIdFromEnv(): String = fromEnv(GCP_PROJECT_ID_VAR)

  private fun locationFromEnv(): VertexAIRegion =
    fromEnv(GCP_LOCATION_VAR).let { envVar ->
      VertexAIRegion.entries.find { it.officialName == envVar }
    }
      ?: throw AIError.Env.GCP(
        nonEmptyListOf(
          "invalid value for $GCP_LOCATION_VAR - valid values are ${VertexAIRegion.entries.map(VertexAIRegion::officialName)}"
        )
      )

  private fun fromEnv(name: String): String =
    getenv(name) ?: throw AIError.Env.GCP(nonEmptyListOf("missing $name env var"))

  val defaultClient = GcpClient(config)

  val CODECHAT by lazy { GcpChat(this, ModelType.TODO("codechat-bison@001")) }
  val TEXT_EMBEDDING_GECKO by lazy { GcpEmbeddings(this, ModelType.TODO("textembedding-gecko")) }

  @JvmField val DEFAULT_CHAT = CODECHAT
  @JvmField val DEFAULT_EMBEDDING = TEXT_EMBEDDING_GECKO

  fun supportedModels(): List<LLM> = listOf(CODECHAT, TEXT_EMBEDDING_GECKO)

  companion object {

    @JvmField val FromEnvironment: GCP = GCP()

    @JvmSynthetic
    suspend inline fun <A> conversation(
      store: VectorStore = LocalVectorStore(FromEnvironment.DEFAULT_EMBEDDING),
      metric: Metric = Metric.EMPTY,
      noinline block: suspend Conversation.() -> A
    ): A = block(conversation(store, metric))

    @JvmSynthetic
    suspend fun <A> conversation(block: suspend Conversation.() -> A): A =
      block(conversation(LocalVectorStore(FromEnvironment.DEFAULT_EMBEDDING)))

    @JvmStatic
    @JvmOverloads
    fun conversation(
      store: VectorStore = LocalVectorStore(FromEnvironment.DEFAULT_EMBEDDING),
      metric: Metric = Metric.EMPTY,
    ): PlatformConversation = Conversation(store, metric)
  }
}
