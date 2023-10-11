package com.xebia.functional.xef.mlflow

import arrow.core.nonEmptyListOf
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.PlatformConversation
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.llm.LLM
import com.xebia.functional.xef.mlflow.models.MLflowChat
import com.xebia.functional.xef.mlflow.models.MLflowCompletion
import com.xebia.functional.xef.mlflow.models.MLflowEmbeddings
import com.xebia.functional.xef.store.LocalVectorStore
import com.xebia.functional.xef.store.VectorStore
import kotlin.jvm.JvmField
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

private const val MLFLOW_GATEWAY_URI = "MLFLOW_GATEWAY_URI"

/**
 * MLflow Gateway integration. Model types are specified in the MLflow configuration
 * and the client needs to specify the route path. An example of a configuration compatible
 * with the model defined is:
 *
 * routes:
 *   - name: completions
 *     route_type: llm/v1/completions
 *     model:
 *       provider: openai
 *       name: gpt-3.5-turbo
 *       config:
 *         openai_api_key: $OPENAI_API_KEY
 *
 *   - name: chat
 *     route_type: llm/v1/chat
 *     model:
 *       provider: openai
 *       name: gpt-3.5-turbo
 *       config:
 *         openai_api_key: $OPENAI_API_KEY
 *
 *   - name: embeddings
 *     route_type: llm/v1/embeddings
 *     model:
 *       provider: openai
 *       name: text-embedding-ada-002
 *       config:
 *         openai_api_key: $OPENAI_API_KEY
 */
class MLflow(gatewayUri: String? = null) {

  private fun gatewayUriFromEnv(): String = fromEnv(MLFLOW_GATEWAY_URI)

  private fun fromEnv(name: String): String =
    getenv(name) ?: throw AIError.Env.MLflow(nonEmptyListOf("missing $name env var"))

  val defaultClient = MlflowClient(gatewayUri ?: gatewayUriFromEnv())

  val COMPLETION by lazy { MLflowCompletion(this, ModelType.TODO("completion")) }
  val CODECHAT by lazy { MLflowChat(this, ModelType.TODO("chat")) }
  val EMBEDDING by lazy { MLflowEmbeddings(this, ModelType.TODO("embeddings")) }

  @JvmField val DEFAULT_COMPLETION = COMPLETION
  @JvmField val DEFAULT_CHAT = CODECHAT
  @JvmField val DEFAULT_EMBEDDING = EMBEDDING

  fun supportedModels(): List<LLM> = listOf(COMPLETION, CODECHAT, EMBEDDING)

  companion object {

    @JvmField val FromEnvironment: MLflow = MLflow()

    @JvmSynthetic
    suspend inline fun <A> conversation(
      store: VectorStore,
      noinline block: suspend Conversation.() -> A
    ): A = block(conversation(store))

    @JvmSynthetic
    suspend fun <A> conversation(block: suspend Conversation.() -> A): A =
      block(conversation(LocalVectorStore(FromEnvironment.DEFAULT_EMBEDDING)))

    @JvmStatic
    @JvmOverloads
    fun conversation(
      store: VectorStore = LocalVectorStore(FromEnvironment.DEFAULT_EMBEDDING)
    ): PlatformConversation = Conversation(store)
  }
}

suspend inline fun <A> MLflow.conversation(noinline block: suspend Conversation.() -> A): A =
  block(Conversation(LocalVectorStore(DEFAULT_EMBEDDING)))
