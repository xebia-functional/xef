package com.xebia.functional.xef.gcp

import arrow.core.nonEmptyListOf
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.PlatformConversation
import com.xebia.functional.xef.embeddings.EmbeddingsService
import com.xebia.functional.xef.env.getenv
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

  val CODECHAT by lazy { GcpModel("codechat-bison@001", config) }
  val TEXT_EMBEDDING_GECKO by lazy { GcpModel("textembedding-gecko", config) }

  @JvmField val DEFAULT_CHAT = CODECHAT
  @JvmField val DEFAULT_EMBEDDING = TEXT_EMBEDDING_GECKO

  fun supportedModels(): List<GcpModel> = listOf(CODECHAT, TEXT_EMBEDDING_GECKO)

  companion object {

    @JvmField val FromEnvironment: GCP = GCP()

    @JvmSynthetic
    suspend inline fun <A> conversation(
      store: VectorStore,
      noinline block: suspend Conversation.() -> A
    ): A = block(conversation(store))

    @JvmSynthetic
    suspend fun <A> conversation(block: suspend Conversation.() -> A): A =
      block(conversation(LocalVectorStore(EmbeddingsService(FromEnvironment.DEFAULT_EMBEDDING))))

    @JvmStatic
    @JvmOverloads
    fun conversation(
      store: VectorStore = LocalVectorStore(EmbeddingsService(FromEnvironment.DEFAULT_EMBEDDING))
    ): PlatformConversation = Conversation(store)
  }
}

suspend inline fun <A> GCP.conversation(noinline block: suspend Conversation.() -> A): A =
  block(Conversation(LocalVectorStore(EmbeddingsService(DEFAULT_EMBEDDING))))
