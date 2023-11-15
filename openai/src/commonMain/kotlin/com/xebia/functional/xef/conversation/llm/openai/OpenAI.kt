package com.xebia.functional.xef.conversation.llm.openai

import arrow.core.nonEmptyListOf
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.aallam.openai.api.exception.InvalidRequestException
import com.aallam.openai.api.finetuning.FineTuningId
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI as OpenAIClient
import com.aallam.openai.client.OpenAIHost
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.AutoClose
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.PlatformConversation
import com.xebia.functional.xef.conversation.autoClose
import com.xebia.functional.xef.conversation.llm.openai.models.*
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.llm.LLM
import com.xebia.functional.xef.metrics.Metric
import com.xebia.functional.xef.store.LocalVectorStore
import com.xebia.functional.xef.store.VectorStore
import kotlin.jvm.JvmField
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private const val KEY_ENV_VAR = "OPENAI_TOKEN"
private const val HOST_ENV_VAR = "OPENAI_HOST"

class OpenAI(
  internal var token: String,
  internal var host: String? = null,
  internal var timeout: Timeout = Timeout.default()
) : AutoCloseable, AutoClose by autoClose() {

  class Timeout(
    val request: Duration,
    val connect: Duration,
    val socket: Duration,
  ) {
    companion object {
      private val REQUEST_TIMEOUT = 60.seconds
      private val CONNECT_TIMEOUT = 10.minutes
      private val SOCKET_TIMEOUT = 10.minutes

      fun default(): Timeout = Timeout(REQUEST_TIMEOUT, CONNECT_TIMEOUT, SOCKET_TIMEOUT)
    }
  }

  private fun openAIHostFromEnv(): String? {
    return getenv(HOST_ENV_VAR)
  }

  fun getToken(): String {
    return token
  }

  fun getHost(): String? {
    return host
  }

  init {
    if (host == null) {
      host = openAIHostFromEnv()
    }
  }

  internal val defaultClient =
    OpenAIClient(
        host = getHost()?.let { OpenAIHost(it) } ?: OpenAIHost.OpenAI,
        token = getToken(),
        logging = LoggingConfig(LogLevel.None),
        headers = mapOf("Authorization" to " Bearer ${getToken()}"),
        timeout = this.timeout.toOAITimeout()
      )
      .let { autoClose(it) }

  val GPT_4 by lazy { autoClose(OpenAIChat(this, ModelType.GPT_4)) }

  val GPT_4_0314 by lazy {
    autoClose(OpenAIFunChat(this, ModelType.GPT_4_0314)) // legacy
  }

  val GPT_4_32K by lazy { autoClose(OpenAIChat(this, ModelType.GPT_4_32K)) }

  val GPT_3_5_TURBO by lazy { autoClose(OpenAIChat(this, ModelType.GPT_3_5_TURBO)) }

  val GPT_3_5_TURBO_16K by lazy { autoClose(OpenAIChat(this, ModelType.GPT_3_5_TURBO_16_K)) }

  val GPT_3_5_TURBO_FUNCTIONS by lazy {
    autoClose(OpenAIFunChat(this, ModelType.GPT_3_5_TURBO_FUNCTIONS))
  }

  val GPT_3_5_TURBO_0301 by lazy {
    autoClose(OpenAIChat(this, ModelType.GPT_3_5_TURBO)) // legacy
  }

  val TEXT_DAVINCI_003 by lazy { autoClose(OpenAICompletion(this, ModelType.TEXT_DAVINCI_003)) }

  val TEXT_DAVINCI_002 by lazy { autoClose(OpenAICompletion(this, ModelType.TEXT_DAVINCI_002)) }

  val TEXT_CURIE_001 by lazy {
    autoClose(OpenAICompletion(this, ModelType.TEXT_SIMILARITY_CURIE_001))
  }

  val TEXT_BABBAGE_001 by lazy { autoClose(OpenAICompletion(this, ModelType.TEXT_BABBAGE_001)) }

  val TEXT_ADA_001 by lazy { autoClose(OpenAICompletion(this, ModelType.TEXT_ADA_001)) }

  val TEXT_EMBEDDING_ADA_002 by lazy {
    autoClose(OpenAIEmbeddings(this, ModelType.TEXT_EMBEDDING_ADA_002))
  }

  val DALLE_2 by lazy { autoClose(OpenAIImages(this, ModelType.GPT_3_5_TURBO)) }

  @JvmField val DEFAULT_CHAT = GPT_3_5_TURBO_16K

  @JvmField val DEFAULT_SERIALIZATION = GPT_3_5_TURBO_FUNCTIONS

  @JvmField val DEFAULT_EMBEDDING = TEXT_EMBEDDING_ADA_002

  @JvmField val DEFAULT_IMAGES = DALLE_2

  /** Returns a list of all publicly available, supported models. */
  fun supportedModels(): List<LLM> = // TODO: impl of abstract provider function
  listOf(
      GPT_4,
      GPT_4_0314,
      GPT_4_32K,
      GPT_3_5_TURBO,
      GPT_3_5_TURBO_16K,
      GPT_3_5_TURBO_FUNCTIONS,
      GPT_3_5_TURBO_0301,
      TEXT_DAVINCI_003,
      TEXT_DAVINCI_002,
      TEXT_CURIE_001,
      TEXT_BABBAGE_001,
      TEXT_ADA_001,
      TEXT_EMBEDDING_ADA_002,
      DALLE_2,
    )

  /**
   * Spawns a model by its [modelId]. It should have the same capabilities as [baseModel]. The model
   * to spawn can i.e. be a fine-tuned model which is not known to the public.
   *
   * Warning: Throws an error at runtime during querying if the model does not provide the same
   * capabilities as [baseModel].
   */
  suspend fun <T : LLM> spawnModel(modelId: String, baseModel: T) =
    either { // TODO: impl of abstract provider function
      ensure(modelExists(modelId)) { "model $modelId not found" }
      @Suppress("UNCHECKED_CAST")
      baseModel.copy(ModelType.FineTunedModel(modelId, baseModel = baseModel.modelType)) as? T
        ?: error("${baseModel::class} does not follow contract to return the most specific type")
    }

  /**
   * Spawns a model based off a [fineTuningJobId]. It should have the same capabilities as
   * [baseModel].
   *
   * This function is safer than [spawnModel] because it checks if the base model the fine-tuned
   * model was derived from matches [baseModel].
   */
  suspend fun <T : LLM> spawnFineTunedModel(fineTuningJobId: String, baseModel: T) = either {
    val job = defaultClient.fineTuningJob(FineTuningId(fineTuningJobId))
    ensureNotNull(job) { "job $fineTuningJobId not found" }
    val fineTunedModel = job.fineTunedModel
    ensureNotNull(fineTunedModel) { "fine tuned model not available, status ${job.status}" }
    ensure(baseModel.modelType.name == job.model.id) {
      "base model instance does not match the job's base model"
    }
    spawnModel(fineTunedModel.id, baseModel).bind()
  }

  /** Checks if the model exists. */
  private suspend fun modelExists(
    modelId: String
  ): Boolean { // TODO: impl of abstract provider function
    val model =
      try {
        defaultClient.model(ModelId(modelId))
      } catch (e: InvalidRequestException) {
        when (e.error.detail?.code) {
          "model_not_found" -> return false
          else -> throw e
        }
      }
    return true
  }

  companion object {

    @JvmStatic
    fun fromEnvironment(): OpenAI {
      val token =
        getenv(KEY_ENV_VAR)
          ?: throw AIError.Env.OpenAI(nonEmptyListOf("missing $KEY_ENV_VAR env var"))
      val host = getenv(HOST_ENV_VAR)
      return OpenAI(token, host)
    }

    @JvmSynthetic
    suspend inline fun <A> conversation(
      store: VectorStore = LocalVectorStore(fromEnvironment().DEFAULT_EMBEDDING),
      metric: Metric = Metric.EMPTY,
      noinline block: suspend Conversation.() -> A
    ): A = block(conversation(store, metric))

    @JvmSynthetic
    suspend fun <A> conversation(block: suspend Conversation.() -> A): A = block(conversation())

    @JvmStatic
    @JvmOverloads
    fun conversation(
      store: VectorStore = LocalVectorStore(fromEnvironment().DEFAULT_EMBEDDING),
      metric: Metric = Metric.EMPTY
    ): PlatformConversation = Conversation(store, metric)
  }
}
